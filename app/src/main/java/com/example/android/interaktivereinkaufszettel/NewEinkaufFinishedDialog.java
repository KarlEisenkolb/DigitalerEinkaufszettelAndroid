package com.example.android.interaktivereinkaufszettel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.android.interaktivereinkaufszettel.geldmanagment.Rechnung;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Context.MODE_PRIVATE;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.FIRESTORE_EINKAUFSZETTEL_CATEGORY_NAME;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.SHARED_PREF;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.SHARED_PREF_STANDARD_EINKAUFSNAME;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Rechnung.RECHNUNG_GEKAUFT;

public class NewEinkaufFinishedDialog extends DialogFragment {

    private OnDialogFinishedListener listener;
    private int modus;
    private CollectionReference collectionEinkaufszettelBillReference;
    private String nutzerUndKauefer;
    private String kategorieName;
    private long kategorieType;
    private Rechnung rechnung;

    public static NewEinkaufFinishedDialog newInstance(String nutzerUndKauefer, OnDialogFinishedListener onDialogFinishedListener) {
        NewEinkaufFinishedDialog f = new NewEinkaufFinishedDialog();
        f.setOnDialogFinishedListener(onDialogFinishedListener);
        Bundle bundle = new Bundle();
        bundle.putString(Rechnung.KAUEFER, nutzerUndKauefer);
        f.setArguments(bundle);
        return f;
    }

    public interface OnDialogFinishedListener{
        void onDialogFinished(DocumentReference docRef);
    }

    private void setOnDialogFinishedListener(OnDialogFinishedListener onDialogFinishedListener){ this.listener = onDialogFinishedListener;}
    private void setDialogModus(int modus) {
        this.modus = modus;
    }
    private void setRechnung(Rechnung rechnung) {
        this.rechnung = rechnung;
    }

    private float dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        nutzerUndKauefer = getArguments().getString(Rechnung.KAUEFER); // Für Hinzufügen

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        collectionEinkaufszettelBillReference = firebaseFirestore.collection(FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION); // Für Hinzufügen/Updaten/Löschen

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView contentTextView = new TextView(getContext());
        contentTextView.setPadding((int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        contentTextView.setText("Bezeichnung:");
        contentTextView.setTextSize((int) dpToPx(getContext(), 10));

        final EditText editContentText = new EditText(getContext());
        editContentText.setPadding((int) dpToPx(getContext(), 20),0,(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        editContentText.setHint("Benötigt für Geplant und Gekauft");
        editContentText.setInputType(InputType.TYPE_CLASS_TEXT);

        final TextView preisTextView = new TextView(getContext());
        preisTextView.setPadding((int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        preisTextView.setText("Preis oder Zahlungsbetrag:");
        preisTextView.setTextSize((int) dpToPx(getContext(), 10));

        final EditText editPreisText = new EditText(getContext());
        editPreisText.setPadding((int) dpToPx(getContext(), 20),0,(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        editPreisText.setHint("Preis oder Zahlung eingeben ...");
        editPreisText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        final String bezeichnung_fuer_einkauf = sharedPreferences.getString(SHARED_PREF_STANDARD_EINKAUFSNAME, SHARED_PREF_STANDARD_EINKAUFSNAME);
        editContentText.setText(bezeichnung_fuer_einkauf);

        linearLayout.addView(contentTextView); // Hinzufügen
        linearLayout.addView(editContentText);
        linearLayout.addView(preisTextView);
        linearLayout.addView(editPreisText);

            builder
                    .setView(linearLayout)
                    .setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final DocumentReference docRef = collectionEinkaufszettelBillReference.document();
                            if (isNotEmpty(editPreisText) && isNotEmpty(editContentText))
                                docRef.set(new Rechnung(editContentText.getText().toString(), nutzerUndKauefer, FIRESTORE_EINKAUFSZETTEL_CATEGORY_NAME, Double.valueOf(editPreisText.getText().toString()), System.currentTimeMillis(), RECHNUNG_GEKAUFT, docRef.getId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(SHARED_PREF_STANDARD_EINKAUFSNAME, editContentText.getText().toString());
                                        editor.apply();
                                        listener.onDialogFinished(docRef);
                                    }
                                });
                            else
                                Toast.makeText(getContext(), "Leere Felder oder falsche Eingabe", Toast.LENGTH_LONG).show();

                        }
                    });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog){}

    private boolean isNotEmpty(EditText view) {
        return view.getText().toString().trim().length() != 0;
    }
}
