package com.example.privatekeyboard.Data;

import org.apache.commons.net.util.Base64;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CipherDecrypt {
    private String text;
    private String encrypted;
    public CipherDecrypt(String text){
        this.text = text;
        encrypt();
    }
    public String getText(){
        return this.text;
    }
    public String getEncrypted(){
        return this.encrypted;
    }

    public static final byte[] KEY = {'P', 'R', 'I', 'V', 'A', 'T', 'E', 'K', 'E', 'Y', 'B', 'O', 'A', 'R', 'D', 'S'};

    private static Cipher ecipher;
    private static Cipher dcipher;

    static {
        try {
            ecipher = Cipher.getInstance("AES");
            SecretKeySpec eSpec = new SecretKeySpec(KEY, "AES");
            ecipher.init(Cipher.ENCRYPT_MODE, eSpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        try {
            dcipher = Cipher.getInstance("AES");
            SecretKeySpec dSpec = new SecretKeySpec(KEY, "AES");
            dcipher.init(Cipher.DECRYPT_MODE, dSpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void encrypt() {
        byte[] b1 = text.getBytes();
        byte[] encryptedValue;
        try {
            encryptedValue = ecipher.doFinal(b1);
            this.encrypted = Base64.encodeBase64String(encryptedValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String decrypt(String encryptedValue) {
        byte[] decryptedValue = Base64.decodeBase64(encryptedValue.getBytes());
        byte[] decValue;
        try {
            decValue = dcipher.doFinal(decryptedValue);
            return new String(decValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}