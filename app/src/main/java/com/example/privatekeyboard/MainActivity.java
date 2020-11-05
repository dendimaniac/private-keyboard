package com.example.privatekeyboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.privatekeyboard.Data.CipherDecrypt;
import com.example.privatekeyboard.Data.ConfirmQRScan;
import com.example.privatekeyboard.Data.NewMessage;
import com.example.privatekeyboard.Helpers.QRUtils;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = (LinearLayout) findViewById(R.id.input_layout);
        StringBuilder query = new StringBuilder("[");
        for (int i = 0; i < layout.getChildCount(); i++) {
            LinearLayout inputFieldItem = (LinearLayout) layout.getChildAt(i);
            TextView label = (TextView) inputFieldItem.getChildAt(0);
            EditText input = (EditText) inputFieldItem.getChildAt(1);

            query.append("{");
            if (input.getInputType() == InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PERSON_NAME) {
                query.append("\"type\":\"text\",");
            } else if (input.getInputType() == InputType.TYPE_CLASS_PHONE) {
                query.append("\"type\":\"tel\",");
            } else if (input.getInputType() == InputType.TYPE_DATETIME_VARIATION_DATE + InputType.TYPE_CLASS_DATETIME) {
                query.append("\"type\":\"date\",");
            }

            query.append("\"label\":\"").append(label.getText()).append("\",");

            CharSequence hint = input.getHint() == null ? "" : input.getHint();
            query.append("\"placeholder\":\"").append(hint).append("\"");
            query.append("}");

            if (i < layout.getChildCount() - 1) {
                query.append(",");
            }
        }
        query.append("]");

        CipherDecrypt test = new CipherDecrypt(query.toString());
        Log.d("EncryptedTestText", test.getText());
        Log.d("EncryptedTestEncrypt", test.getEncrypted());
        Log.d("EncryptedTestDecrypt", test.decrypt(test.getEncrypted()));

        Log.d("InputCombined", query.toString());
        HubConnection hubConnection = HubConnectionBuilder.create("https://privatekeyboard.azurewebsites.net/api").build();
        // In development, change the ip to the ip of the machine running the function app
//        HubConnection hubConnection = HubConnectionBuilder.create("http://192.168.1.149:7071/api").build();

        Log.d("InputType", String.valueOf(((EditText) findViewById(R.id.sendMessageTextField)).getInputType() == InputType.TYPE_TEXT_VARIATION_PERSON_NAME + InputType.TYPE_CLASS_TEXT));
        Log.d("InputType", String.valueOf(((EditText) findViewById(R.id.sendMessageTextField)).getInputType()));
        hubConnection.on("newMessage", (message) -> {
            Log.d("NewMessage", message.text);
            if (!message.uuid.equals(QRUtils.connectedUuid)) return;

            runOnUiThread(() -> ((EditText) findViewById(R.id.sendMessageTextField)).setText(message.text));
        }, NewMessage.class);

        hubConnection.on("confirmQRScan", (message) -> {
            Log.d("ConfirmQRScan", message.uuid);
            if (!message.uuid.equals(QRUtils.newUuid)) return;

            QRUtils.connectedUuid = message.uuid;
            QRUtils.SetNewQRBitmap((ImageView) findViewById(R.id.qrImage), (LinearLayout) findViewById(R.id.input_layout));
        }, ConfirmQRScan.class);

        hubConnection.start().blockingAwait();

        QRUtils.SetNewQRBitmap((ImageView) findViewById(R.id.qrImage), (LinearLayout) findViewById(R.id.input_layout));

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