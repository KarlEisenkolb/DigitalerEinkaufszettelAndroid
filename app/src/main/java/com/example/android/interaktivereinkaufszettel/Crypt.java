package com.example.android.interaktivereinkaufszettel;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {

    final String TAG = "CRYPTCLASS";
    private final String firebaseStringKey = "oChvIXgFu9BqlaujP/0aT7j8WC/c02KuQxRnNmAwq5k="; // Hier neuen Key einfügen

    public Crypt() {

        /*KeyGenerator keygen;
        try {
            keygen = KeyGenerator.getInstance("AES");
            keygen.init(256);
            SecretKey key = keygen.generateKey();
            String stringToCopyKey = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
            Log.d(TAG, "Crypt Constructor, keyToString: " +stringToCopyKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/




    }

     public byte[] encrypt(String string){

     }

    public int decrypt(byte[] bytes){

    }

        "freunde".charAt(0);

        String stringText = "Hallo was geht ab!";
        byte[] byteText = stringText.getBytes();

        try {

            byte[] encodedKey = Base64.decode(firebaseStringKey, Base64.DEFAULT);
            SecretKey firebaseKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

            Cipher cipher_en = Cipher.getInstance("AES/GCM/NoPadding");
            cipher_en.init(Cipher.ENCRYPT_MODE, firebaseKey);
            byte[] cipherText = cipher_en.doFinal(byteText);
            byte[] iv = cipher_en.getIV();

            Log.d(TAG, "Crypt Constructor: " +cipherText);

            // Entschluesseln
            Cipher cipher_de = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher_de.init(Cipher.DECRYPT_MODE, firebaseKey, spec);
            byte[] cipherData2 = cipher_de.doFinal(cipherText);
            String erg = new String(cipherData2);

            Log.d(TAG, "Crypt Constructor: " +erg);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            Log.d(TAG, "Try-Catch Failed!");
        }

}
