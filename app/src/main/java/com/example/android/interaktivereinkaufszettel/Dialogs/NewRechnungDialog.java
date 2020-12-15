package com.example.android.interaktivereinkaufszettel.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung.RECHNUNG_GEKAUFT;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung.RECHNUNG_GEPLANT;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung.RECHNUNG_ZAHLUNG;

public class NewRechnungDialog extends DialogFragment {

    final private static int MODUS_ADD = 0;
    final private static int MODUS_UPDATE = 1;

    private OnDialogFinishedListener listener;
    private int modus;
    private CollectionReference collectionIndividualBillReference;
    private String nutzerUndKauefer;
    private String kategorieName;
    private long kategorieType;
    private Rechnung rechnung;

    public static NewRechnungDialog newAddInstance(Bundle placeholderFragmentBundleCategoryID, String nutzerUndKauefer, OnDialogFinishedListener onDialogFinishedListener) {
        NewRechnungDialog f = new NewRechnungDialog();
        f.setDialogModus(MODUS_ADD);
        f.setOnDialogFinishedListener(onDialogFinishedListener);
        Bundle bundle = new Bundle();
        bundle.putAll(placeholderFragmentBundleCategoryID); //Kategorie Name, Kategorie ID, Kategorie Type
        bundle.putString(Rechnung.KAUEFER, nutzerUndKauefer);
        f.setArguments(bundle);
        return f;
    }

    public static NewRechnungDialog newUpdateInstance(Rechnung rechnung, String categoryID, OnDialogFinishedListener onDialogFinishedListener) {
        NewRechnungDialog f = new NewRechnungDialog();
        f.setDialogModus(MODUS_UPDATE);
        f.setOnDialogFinishedListener(onDialogFinishedListener);
        f.setRechnung(rechnung);
        Bundle bundle = new Bundle();
        bundle.putString(Category.ID, categoryID);
        f.setArguments(bundle);
        return f;
    }

    public interface OnDialogFinishedListener{
        void onDialogFinished();
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
        if (getArguments() != null && modus == MODUS_ADD) {
            kategorieName = getArguments().getString(Category.NAME); // Für Hinzufügen
            kategorieType = getArguments().getLong(Category.TYPE); // Für Hinzufügen
            nutzerUndKauefer = getArguments().getString(Rechnung.KAUEFER); // Für Hinzufügen
        }

        String categoryID = getArguments().getString(Category.ID);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        collectionIndividualBillReference = firebaseFirestore.collection(categoryID); // Für Hinzufügen/Updaten/Löschen

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.setPadding((int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),0);
        final RadioButton radioButtonGekauft = new RadioButton(getContext());
        radioButtonGekauft.setText("Gekauft");
        radioButtonGekauft.setPadding(0,0,(int) dpToPx(getContext(), 15),0);
        final RadioButton radioButtonGeplant = new RadioButton(getContext());
        radioButtonGeplant.setText("Geplant");
        radioButtonGeplant.setPadding(0,0,(int) dpToPx(getContext(), 15),0);
        final RadioButton radioButtonZahlung = new RadioButton(getContext());
        radioButtonZahlung.setText("Zahlung");
        radioButtonZahlung.setPadding(0,0,(int) dpToPx(getContext(), 15),0);

        if (modus == MODUS_UPDATE) {
            if (rechnung.gibType() == RECHNUNG_GEKAUFT || rechnung.gibType() == RECHNUNG_GEPLANT) {
                radioGroup.addView(radioButtonGekauft);
                radioGroup.addView(radioButtonGeplant);
            }
        }else{ // MODUS_ADD
            if (kategorieType == Category.CATEGORY_GROUP_LIST) {
                radioGroup.addView(radioButtonGekauft);
                radioGroup.addView(radioButtonGeplant);
                radioGroup.addView(radioButtonZahlung);
            }else { // CATEGORY_SOLO_LIST
                radioGroup.addView(radioButtonGekauft);
                radioGroup.addView(radioButtonGeplant);
            }
        }

        final TextView contentTextView = new TextView(getContext());
        contentTextView.setPadding((int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        contentTextView.setText("Bezeichnung:");
        contentTextView.setTextSize((int) dpToPx(getContext(), 10));

        final EditText editContentText = new EditText(getContext());
        editContentText.setPadding((int) dpToPx(getContext(), 20),0,(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        editContentText.setHint("Benötigt für Geplant/Gekauft/Zahlung");
        editContentText.setInputType(InputType.TYPE_CLASS_TEXT);

        final TextView preisTextView = new TextView(getContext());
        preisTextView.setPadding((int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        preisTextView.setText("Preis oder Zahlungsbetrag:");
        preisTextView.setTextSize((int) dpToPx(getContext(), 10));

        final EditText editPreisText = new EditText(getContext());
        editPreisText.setPadding((int) dpToPx(getContext(), 20),0,(int) dpToPx(getContext(), 20),(int) dpToPx(getContext(), 20));
        editPreisText.setHint("Kaufpreis oder Zahlungsbetrag...");
        editPreisText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

        linearLayout.addView(radioGroup);

        if (modus == MODUS_UPDATE) {
                editContentText.setText(rechnung.gibContent());
                editPreisText.setText(""+rechnung.gibPreis());
                linearLayout.addView(contentTextView);
                linearLayout.addView(editContentText);
                linearLayout.addView(preisTextView);
                linearLayout.addView(editPreisText);

        }else{ // MODUS_ADD
            linearLayout.addView(contentTextView);
            linearLayout.addView(editContentText);
            linearLayout.addView(preisTextView);
            linearLayout.addView(editPreisText);
        }


        if (modus == MODUS_ADD) {
            builder
                    .setView(linearLayout)
                    .setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            long RadioButtonState = RECHNUNG_GEKAUFT;
                            if (radioButtonGeplant.isChecked()) {
                                RadioButtonState = RECHNUNG_GEPLANT;
                            } else if (radioButtonZahlung.isChecked()) {
                                RadioButtonState = RECHNUNG_ZAHLUNG;
                            }
                            DocumentReference docRef = collectionIndividualBillReference.document();
                            if (isNotEmpty(editPreisText) && isNotEmpty(editContentText) && (radioButtonGeplant.isChecked() || radioButtonGekauft.isChecked() || radioButtonZahlung.isChecked()))
                                docRef.set(new Rechnung(editContentText.getText().toString(), nutzerUndKauefer, kategorieName, Double.valueOf(editPreisText.getText().toString()), System.currentTimeMillis(), RadioButtonState, docRef.getId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        listener.onDialogFinished();
                                    }
                                });
                            else
                                Toast.makeText(getContext(), "Fehler: Leere Felder", Toast.LENGTH_LONG).show();

                        }
                    });
        }else{ // MODUS_UPDATE
            builder
                    .setView(linearLayout)
                    .setPositiveButton("Updaten", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (rechnung.gibType() != RECHNUNG_ZAHLUNG) { // Gekauft oder Geplant
                                if (isNotEmpty(editPreisText) && isNotEmpty(editContentText) && (radioButtonGeplant.isChecked() || radioButtonGekauft.isChecked())) {
                                    long RadioButtonState = RECHNUNG_GEKAUFT;
                                    if (radioButtonGeplant.isChecked())
                                        RadioButtonState = RECHNUNG_GEPLANT;
                                    collectionIndividualBillReference.document(rechnung.gibId()).set(new Rechnung(editContentText.getText().toString(), rechnung.gibKauefer(), rechnung.gibKategorie(), Double.valueOf(editPreisText.getText().toString()), System.currentTimeMillis(), RadioButtonState, rechnung.gibId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            listener.onDialogFinished();
                                        }
                                    });
                                } else
                                    Toast.makeText(getContext(), "Fehler: Leere Felder", Toast.LENGTH_LONG).show();
                            }else{ // Zahlung
                                if (isNotEmpty(editPreisText) && isNotEmpty(editContentText)) {
                                    collectionIndividualBillReference.document(rechnung.gibId()).set(new Rechnung(editContentText.getText().toString(), rechnung.gibKauefer(), rechnung.gibKategorie(), Double.valueOf(editPreisText.getText().toString()), rechnung.gibDatum(), rechnung.gibType(), rechnung.gibId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            listener.onDialogFinished();
                                        }
                                    });
                                } else
                                    Toast.makeText(getContext(), "Fehler: Leere Felder", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton("Löschen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            collectionIndividualBillReference.document(rechnung.gibId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    listener.onDialogFinished();
                                }
                            });
                        }
                    });
        }
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog){}

    private boolean isNotEmpty(EditText view) {
        return view.getText().toString().trim().length() != 0;
    }
}
