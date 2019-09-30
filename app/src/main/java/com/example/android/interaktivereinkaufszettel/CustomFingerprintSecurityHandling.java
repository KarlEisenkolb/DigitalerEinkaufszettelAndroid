package com.example.android.interaktivereinkaufszettel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class CustomFingerprintSecurityHandling {

    final static String PASSPHRASE = "passphrase";
    final static int RC_SIGN_IN = 0;
    private static final String TAG = "fingerprintTest";

    public interface FingerprintSuccessListener{
        void onFingerprintSuccess();
    }

    public CustomFingerprintSecurityHandling(FragmentActivity mainActivity, FingerprintSuccessListener listener) {
        final FingerprintSuccessListener fingerprintSuccessListener = listener;
        //Create a thread pool with a single thread//
        Executor newExecutor = Executors.newSingleThreadExecutor();

        //Start listening for authentication events//
        final BiometricPrompt myBiometricPrompt = new BiometricPrompt(mainActivity, newExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            //onAuthenticationError is called when a fatal error occurrs//
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Log.d(TAG, "Negative Button Pushed");
                } else {
                    //Print a message to Logcat//
                    Log.d(TAG, "An unrecoverable error occurred");
                }
            }

            //onAuthenticationSucceeded is called when a fingerprint is matched successfully//
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Fingerprint recognised successfully");
                fingerprintSuccessListener.onFingerprintSuccess();
            }

            //onAuthenticationFailed is called when the fingerprint doesn’t match//
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "Fingerprint not recognised");
            }
        });

        //Create the BiometricPrompt instance//
        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("EinkaufsApp")
                .setSubtitle("Identifikationsvorgang")
                .setDescription("Fingerabdrucküberprüfung")
                .setNegativeButtonText("Abbruch")
                .build();

        myBiometricPrompt.authenticate(promptInfo);
    }

    /*private void startPasswordDialog(){
        PassphrasenDialog passphrasenDialog = PassphrasenDialog.newInstance(MainActivity.this, new PassphrasenDialog.OnDialogFinishedListener() {
            @Override
            public void onDialogFinished(String passphrase) {
                Intent intent = new Intent(MainActivity.this, KeystoreActivity.class);
                intent.putExtra(PASSPHRASE, passphrase);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });
        passphrasenDialog.show(getSupportFragmentManager(), "PassphrasenDialog");
    }*/

}
