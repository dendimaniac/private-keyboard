package com.example.privatekeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.privatekeyboard.Data.ConfirmQRScan;
import com.example.privatekeyboard.Data.NewCheckRadio;
import com.example.privatekeyboard.Data.NewMessage;
import com.example.privatekeyboard.Helpers.ConvertImage;
import com.example.privatekeyboard.Helpers.QRUtils;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final String functionUrl = "https://privatekeyboard.azurewebsites.net/api";
    private LinearLayout linearLayout;
    // Deployment function URL: https://privatekeyboard.azurewebsites.net/api
    // Development function URL (example): http://192.168.1.149:7071/api

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SdCardPath")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = (LinearLayout) findViewById(R.id.input_layout);
        HubConnection hubConnection = HubConnectionBuilder.create(functionUrl).build();
        // Init folder chứa local files để test convert từ ảnh qua string
        // Lấy ảnh trong storage hoặc chỗ khác thì phải xin permission. Thêm rắc rối. Dù sao cũng lấy string từ web app rồi thả vào storage.
        // Client có nhu cầu save thì chờ lấy code base bên kia rồi edit sau.
        try {
            FileOutputStream fos = openFileOutput("hi",0);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // View -> Tool Windows -> Device Manager search private keyboard, upload 1 ảnh random vào folder files để test
        String chaulenba = ConvertImage.convertImageToString("/data/data/com.example.privatekeyboard/files/48423700_389535721816795_8993131097952878592_n.jpg");
        Log.d("ConvertedImage",chaulenba);
        //ConvertImage.convertStringToImageByteArray(a);
        try {
            ConvertImage.convertStringToImageByteArray(chaulenba);
            Log.d("ConvertedString","ggez");
        }catch (Exception e){
            Log.d("ErrorImg", String.valueOf(e));
        }




        hubConnection.on("newMessage", (message) -> {
            Log.d("NewMessage", message.text);
            if (!message.sender.equals(QRUtils.connectedUuid)) return;

            LinearLayout inputField = (LinearLayout) linearLayout.getChildAt(message.targetInput);
            runOnUiThread(() -> ((EditText) inputField.getChildAt(1)).setText(message.text));
        }, NewMessage.class);

        hubConnection.on("newCheckRadio", (message) -> {
            Log.d("NewCheckRadio", String.valueOf(message.targetRadioButton));
            if (!message.sender.equals(QRUtils.connectedUuid)) return;

            LinearLayout fieldLinearLayout = (LinearLayout) linearLayout.getChildAt(message.targetRadioGroup);
            RadioGroup radioGroup = (RadioGroup) fieldLinearLayout.getChildAt(1);
            runOnUiThread(() -> ((RadioButton) radioGroup.getChildAt(message.targetRadioButton)).setChecked(true));
        }, NewCheckRadio.class);

        hubConnection.on("confirmQRScan", (message) -> {
            Log.d("ConfirmQRScan", message.uuid);
            if (!message.uuid.equals(QRUtils.newUuid)) return;

            QRUtils.connectedUuid = message.uuid;
            QRUtils.SetNewQRBitmap((ImageView) findViewById(R.id.qrImage), linearLayout);
        }, ConfirmQRScan.class);

        hubConnection.start().blockingAwait();

        QRUtils.SetNewQRBitmap((ImageView) findViewById(R.id.qrImage), linearLayout);

        ((EditText) findViewById(R.id.sendMessageTextField)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("TextChanged", hubConnection.getConnectionState().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}