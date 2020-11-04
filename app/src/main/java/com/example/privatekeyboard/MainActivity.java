package com.example.privatekeyboard;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.privatekeyboard.Data.CipherDecrypt;
import com.example.privatekeyboard.Data.ConfirmQRScan;
import com.example.privatekeyboard.Data.InputForm;
import com.example.privatekeyboard.Data.NewMessage;
import com.google.zxing.WriterException;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity {
    private String connectedUuid;
    private String newUuid;
    private ArrayList<InputForm> formData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String codeText = "renmai";
        String codeEncrypted = CipherDecrypt.encrypt(codeText);
        Log.d("Encrypted",codeEncrypted);
        Log.d("Decrypted",CipherDecrypt.decrypt(codeEncrypted));
        CipherDecrypt test = new CipherDecrypt("RenMaiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
        try {
            //test.decrypt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HubConnection hubConnection = HubConnectionBuilder.create("https://privatekeyboard.azurewebsites.net/api").build();
        // In development, change the ip to the ip of the machine running the function app
//        HubConnection hubConnection = HubConnectionBuilder.create("http://192.168.1.149:7071/api").build();

        hubConnection.on("newMessage", (message) -> {
            Log.d("NewMessage", message.text);
            if (!message.uuid.equals(connectedUuid)) return;

            runOnUiThread(() -> ((EditText) findViewById(R.id.sendMessageTextField)).setText(message.text));
        }, NewMessage.class);

        hubConnection.on("confirmQRScan", (message) -> {
            Log.d("ConfirmQRScan", message.uuid);
            if (!message.uuid.equals(newUuid)) return;

            connectedUuid = message.uuid;
            SetNewQRBitmap();
        }, ConfirmQRScan.class);

        hubConnection.start().blockingAwait();

        SetNewQRBitmap();

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

    private void SetNewQRBitmap() {
        newUuid = UUID.randomUUID().toString();
        Log.d("NewUUID", newUuid);
        // In development, change the ip to the the ip of the machine running the function app
//        QRGEncoder qrgEncoder = new QRGEncoder("http://192.168.1.149:3000/?amount=3&uuid=" + newUuid, null, QRGContents.Type.TEXT, 400);
        QRGEncoder qrgEncoder = new QRGEncoder("https://lively-stone-01c8fc003.azurestaticapps.net/?amount=3&uuid=" + newUuid, null, QRGContents.Type.TEXT, 400);
        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            ((ImageView) findViewById(R.id.qrImage)).setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}