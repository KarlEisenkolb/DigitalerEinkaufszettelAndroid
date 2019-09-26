package com.example.android.interaktivereinkaufszettel;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import static com.example.android.interaktivereinkaufszettel.MainActivity.RC_SIGN_IN;

public class CustomFirebaseSecurityHandling {

    private Activity mainActivity;

    public CustomFirebaseSecurityHandling(Activity activity) {
        this.mainActivity = activity;

        checkPermission();

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
        } else {
            // Create and launch sign-in intent
            mainActivity.startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }

    }

    // to sign out as menu button
    public void firebaseSignOut(){
        AuthUI.getInstance()
                .signOut(mainActivity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        mainActivity.finish();
                    }
                });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                mainActivity.requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        1);
            }
        }
    }
}
