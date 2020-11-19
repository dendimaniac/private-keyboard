package com.example.privatekeyboard;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.privatekeyboard.Data.ConfirmQRScan;
import com.example.privatekeyboard.Data.EmailConfig;
import com.example.privatekeyboard.Data.NewCheckRadio;
import com.example.privatekeyboard.Data.NewMessage;
import com.example.privatekeyboard.Data.TakingPicture;
import com.example.privatekeyboard.Data.TiltAngle;
import com.example.privatekeyboard.Helpers.QRUtils;
import com.example.privatekeyboard.Helpers.SendMail;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import static com.example.privatekeyboard.Data.EmailConfig.saveInstance;

public class MainActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private String sex = "No response";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, -90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    String fileImage = null;

    private final String functionUrl = "https://privatekeyboard.azurewebsites.net/api";
    private LinearLayout linearLayout;
    private ImageView profileImageView;
    // Deployment function URL: https://privatekeyboard.azurewebsites.net/api
    // Development function URL (example): http://192.168.1.149:7071/api
    // private final ActivityResultLauncher<Void> takePicturePreview = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> ((ImageView) findViewById(R.id.qrImage)).setImageBitmap(result));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onResume(){
        super.onResume();
        linearLayout = findViewById(R.id.input_layout);
        ImageView qrImage = findViewById(R.id.qrImage);
        findViewById(R.id.radioMale).setOnClickListener(v -> {
            sex = "Male";
            Log.d("Radio", sex);
        });
        findViewById(R.id.radioFemale).setOnClickListener(v -> {
            sex = "Female";
            Log.d("Radio", sex);
        });
        profileImageView = findViewById(R.id.takenImage);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                String savedImagePath = bundle.getString("image_path");
                this.fileImage = savedImagePath;
                File file = new File(savedImagePath);
                int size = (int) file.length();
                byte[] bytes = new byte[size];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                profileImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Button sendEmailButton = findViewById(R.id.sendEmailButton);
        Button openCustomCameraButton = findViewById(R.id.buttonCam);

        sendEmailButton.setOnClickListener(view -> {
            sendEmail();
        });


        openCustomCameraButton.setOnClickListener(v -> {
            Log.d("Camera","Clicked");
            saveInstanceNew();
            Intent intent = new Intent(MainActivity.this, CustomCameraActivity.class);
            startActivity(intent);
        });

        linearLayout = findViewById(R.id.input_layout);
        if (!EmailConfig.saveInstanceNew.isEmpty()) {
            getInstance(EmailConfig.saveInstanceNew);
        }
        HubConnection hubConnection = HubConnectionBuilder.create(functionUrl).build();

        hubConnection.on("sendInputField", (message) -> {
            Log.d("NewMessage", message.text);
            if (!message.sender.equals(QRUtils.connectedUuid)) return;
            LinearLayout inputField = (LinearLayout) linearLayout.getChildAt(message.targetInput);
            Log.d("NewMessageTI", message.targetInput.toString());
            runOnUiThread(() -> ((EditText) inputField.getChildAt(1)).setText(message.text));
            saveInstance.put("InputField-" + message.targetInput.toString(), message.text);
        }, NewMessage.class);

        hubConnection.on("selectRadioGroup", (message) -> {
            Log.d("NewCheckRadio", String.valueOf(message.targetRadioButton));
            if (!message.sender.equals(QRUtils.connectedUuid)) return;

            LinearLayout fieldLinearLayout = (LinearLayout) linearLayout.getChildAt(message.targetRadioGroup);
            Log.d("NewMessageRadio", message.targetRadioGroup.toString());
            RadioGroup radioGroup = (RadioGroup) fieldLinearLayout.getChildAt(1);
            runOnUiThread(() -> ((RadioButton) radioGroup.getChildAt(message.targetRadioButton)).setChecked(true));
            if (message.targetRadioButton == 0) {
                saveInstance.put("RadioField-Sex", "radioMale");
                sex = "Male";
            } else {
                saveInstance.put("RadioField-Sex", "radioFemale");
                sex = "Female";
            }
            Log.d("Radio",sex);
            Log.d("RadioSex", message.targetRadioButton.toString());

        }, NewCheckRadio.class);

        hubConnection.on("updateTiltAngle", (message) -> {
            if (!message.sender.equals(QRUtils.connectedUuid)) return;

            Log.d("TiltAngle", String.valueOf(message.value));
            TextView tiltTextView = findViewById(R.id.tiltValue);
            tiltTextView.setText("Angle:" + message.value);
            saveInstance.put("TextViewField-Tilt", message.value.toString());

        }, TiltAngle.class);

        hubConnection.on("takePicture", (message) -> {
            if (!message.sender.equals(QRUtils.connectedUuid)) return;
            Log.d("isTakingPicture", String.valueOf(message.value));
            if(message.value == true) {
                openCustomCameraButton.callOnClick();
                hubConnection.stop();
            }

        }, TakingPicture.class);

        hubConnection.on("confirmQRScan", (message) -> {
            Log.d("ConfirmQRScan", message.uuid);
            if (!message.uuid.equals(QRUtils.newUuid)) return;
            // hide the QR view after connecting successfully
            qrImage.setVisibility(View.INVISIBLE);
            QRUtils.connectedUuid = message.uuid;
            QRUtils.SetNewQRBitmap(findViewById(R.id.qrImage), linearLayout);
        }, ConfirmQRScan.class);

        hubConnection.start().blockingAwait();

        QRUtils.SetNewQRBitmap(findViewById(R.id.qrImage), linearLayout);
        if (QRUtils.connectedUuid != null)
        {
            qrImage.setVisibility(View.INVISIBLE);

        }
    }
    private void saveInstanceNew(){
        EmailConfig.saveInstanceNew.put("RadioField-Sex", "No Response");
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            LinearLayout fieldLayout = (LinearLayout) linearLayout.getChildAt(i);
            String fieldTag = (String) linearLayout.getChildAt(i).getTag();
            if (!fieldTag.equals("hidden")) {

                if (fieldLayout.getChildAt(1) instanceof EditText) {
                    EmailConfig.saveInstanceNew.put("InputField-"+i, ((EditText) fieldLayout.getChildAt(1)).getText().toString().trim());
                } else if (fieldLayout.getChildAt(1) instanceof RadioGroup) {
                    if (((RadioButton) ((RadioGroup) fieldLayout.getChildAt(1)).getChildAt(0)).isChecked())
                        EmailConfig.saveInstanceNew.put("RadioField-Sex", "radioMale");
                    else
                        EmailConfig.saveInstanceNew.put("RadioField-Sex", "radioFemale");
                }

            }
        }
    }
    private void sendEmail() {
        //Getting content for email
        String email = ((EditText) findViewById(R.id.editTextEmail)).getText().toString().trim();
        String subject = "Personal Information";
        String firstname = ((EditText) findViewById(R.id.sendMessageTextField)).getText().toString().trim();
        String lastname = ((EditText) findViewById(R.id.editTextTextPersonName2)).getText().toString().trim();
        String phonenum = ((EditText) findViewById(R.id.editTextTextPersonName3)).getText().toString().trim();


        //Creating SendMail object
        Log.d("RadioMail",sex);

        SendMail sm = new SendMail(this, email, subject, firstname, lastname, phonenum, sex, this.fileImage);

        //Executing sendmail to send email
        sm.execute();
    }

    private void rotateImageToUpright(Bitmap source) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        float angle = ORIENTATIONS.get(rotation);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(MainActivity.this, "Landscape Mode", Toast.LENGTH_LONG).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(MainActivity.this, "Portrait Mode", Toast.LENGTH_LONG).show();
        }
        rotateImageToUpright(((BitmapDrawable) profileImageView.getDrawable()).getBitmap());
    }

    private void getInstance(HashMap<String, String> hashMap) {
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            String[] arrOfStr = key.split("-", 2);
            if (arrOfStr[0].equals("InputField")) {
                LinearLayout inputField = (LinearLayout) linearLayout.getChildAt(Integer.parseInt(arrOfStr[1]));
                ((EditText) inputField.getChildAt(1)).setText(hashMap.get(key));
            } else if ((arrOfStr[0].equals("RadioField"))){
                RadioGroup radio = findViewById(R.id.radioSex);
                switch (hashMap.get(key)) {
                    case "radioMale":
                        radio.check(R.id.radioMale);
                        sex = "Male";
                        break;
                    case "radioFemale":
                        radio.check(R.id.radioFemale);
                        sex = "Female";
                        break;
                }
            }
            else {
                TextView tiltTextView = findViewById(R.id.tiltValue);
                tiltTextView.setText("Angle:" + hashMap.get(key));
            }
        }
    }
}