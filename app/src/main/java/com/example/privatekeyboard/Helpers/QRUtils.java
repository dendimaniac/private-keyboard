package com.example.privatekeyboard.Helpers;

import android.graphics.Bitmap;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.privatekeyboard.Data.CipherDecrypt;
import com.google.zxing.WriterException;

import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRUtils {
    public static String connectedUuid;
    public static String newUuid;
    private static final String baseWebAppUrl = "http://192.168.1.149:3000/";
    // Deployment web app URL: https://lively-stone-01c8fc003.azurestaticapps.net/
    // Development web app URL (example): http://192.168.1.149:3000/

    public static void SetNewQRBitmap(ImageView qrImage, LinearLayout formLinearLayout) {
        newUuid = UUID.randomUUID().toString();
        Log.d("NewUUID", newUuid);
        String settings = GenerateQRQuery(formLinearLayout);
        Log.d("InputSettings", settings);
        QRGEncoder qrgEncoder = new QRGEncoder(baseWebAppUrl + "?settings=" + settings + "&uuid=" + newUuid, null, QRGContents.Type.TEXT, 400);
        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private static String GenerateQRQuery(LinearLayout layout) {
        StringBuilder query = new StringBuilder("[");
        for (int i = 0; i < layout.getChildCount(); i++) {
            LinearLayout inputFieldItem = (LinearLayout) layout.getChildAt(i);
            TextView label = (TextView) inputFieldItem.getChildAt(0);
            EditText input = (EditText) inputFieldItem.getChildAt(1);

            query.append("{");
            query.append("\"position\":\"").append(i).append("\",");
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
        return CipherDecrypt.Encrypt(query.toString());
    }
}
