package com.example.privatekeyboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HubConnection hubConnection = HubConnectionBuilder.create("http://192.168.1.148:7071/api")
                .build();

        hubConnection.on("newMessage", (message) -> {
            Log.d("NewMessage", message);
            runOnUiThread(() -> ((TextView) findViewById(R.id.newMessageTextView)).setText(message));
        }, String.class);

        hubConnection.start().blockingAwait();

        ((EditText)findViewById(R.id.sendMessageTextField)).addTextChangedListener(new TextWatcher() {
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