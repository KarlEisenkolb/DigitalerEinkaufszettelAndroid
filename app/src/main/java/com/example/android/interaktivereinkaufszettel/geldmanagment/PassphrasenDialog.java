package com.example.android.interaktivereinkaufszettel.geldmanagment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class PassphrasenDialog extends DialogFragment {

    private static final String TAG = "fingerprintTest";
    OnDialogFinishedListener listener;
    Activity activity;

    public static PassphrasenDialog newInstance(Activity mainActivity, OnDialogFinishedListener listener) {
        PassphrasenDialog f = new PassphrasenDialog();
        f.setOnDialogFinishedListener(listener);
        f.setMainActivity(mainActivity);
        return f;
    }

    public void setMainActivity(Activity mainActivity) {
        this.activity = mainActivity;
    }

    private void setOnDialogFinishedListener(OnDialogFinishedListener listener){
        this.listener = listener;
    }

    public interface OnDialogFinishedListener{
        void onDialogFinished(String string);
    }

    private float dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(getContext());
        editText.setPadding((int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 30),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        editText.setHint("Passphrase eingeben");
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        linearLayout.addView(editText);
        builder
                .setView(linearLayout)
                .setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isNotEmpty(editText))
                            listener.onDialogFinished(editText.getText().toString());
                        else{
                            Toast.makeText(activity, "Leere Passphrase", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {}

    private boolean isNotEmpty(EditText view) {
        return view.getText().toString().trim().length() != 0;
    }
}
