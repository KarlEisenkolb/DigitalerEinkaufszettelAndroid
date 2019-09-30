package com.example.android.interaktivereinkaufszettel.geldmanagment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.android.interaktivereinkaufszettel.Crypt;
import com.example.android.interaktivereinkaufszettel.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_PASSPHRASE;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.SHARED_PREF_NAME;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Nutzer.ID;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Nutzer.NAME;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.FIRESTORE_NUTZER_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.PASSPHRASE;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.SHARED_PREF;

public class AddEditNutzerAndLists extends AppCompatActivity {

    private EditText nutzer_name_EditTextView;
    private EditText nutzer_gehalt_EditTextView;
    private NutzerAdapter adapter;
    private Nutzer currentNutzer;
    private CollectionReference collectionNutzerReference;
    RecyclerView recyclerView;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_nutzer_and_lists);

        String passphrase = getIntent().getExtras().getString(PASSPHRASE);
        Crypt.initializePassphrase(passphrase);

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionNutzerReference = firebaseFirestore.collection(FIRESTORE_NUTZER_COLLECTION);

        nutzer_name_EditTextView = findViewById(R.id.nutzer_name);
        nutzer_gehalt_EditTextView = findViewById(R.id.nutzer_gehalt);

        FirestoreRecyclerOptions<Nutzer> options = new FirestoreRecyclerOptions.Builder<Nutzer>()
                .setQuery(collectionNutzerReference, Nutzer.class)
                .build();

        recyclerView = findViewById(R.id.recycler_view_nutzer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new NutzerAdapter(options);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new NutzerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nutzer nutzer) {
                nutzer_name_EditTextView.setText(nutzer.gibName());
                nutzer_gehalt_EditTextView.setText(String.valueOf(nutzer.gibGehalt()));
                currentNutzer = nutzer;
            }
        });

    }

    public void nutzer_add(View view){
        if (isNotEmpty(nutzer_name_EditTextView) && isNotEmpty(nutzer_gehalt_EditTextView)) {
            String nutzername =  nutzer_name_EditTextView.getText().toString();
            Double gehalt = Double.valueOf(nutzer_gehalt_EditTextView.getText().toString());
            DocumentReference docRef = collectionNutzerReference.document();
            docRef.set(new Nutzer(nutzername, gehalt, docRef.getId()));
        } else
            Snackbar.make(recyclerView, "Leere Eingabe in Feld", Snackbar.LENGTH_LONG).show();
    }

    public void nutzer_update(View view){
        if (isNotEmpty(nutzer_name_EditTextView) && isNotEmpty(nutzer_gehalt_EditTextView)) {
            Crypt cryptNormal = new Crypt(CRYPT_USE_DEFAULT_KEY);
            Crypt cryptPassphrase = new Crypt(CRYPT_USE_PASSPHRASE);

            WriteBatch batch = firebaseFirestore.batch();
            batch.update(collectionNutzerReference.document(currentNutzer.gibId()), Nutzer.NAME, cryptNormal.encryptString(nutzer_name_EditTextView.getText().toString()));
            batch.update(collectionNutzerReference.document(currentNutzer.gibId()), Nutzer.GEHALT, cryptPassphrase.encryptDouble(Double.valueOf(nutzer_gehalt_EditTextView.getText().toString())));
            batch.commit();
        } else
            Snackbar.make(recyclerView, "Leere Eingabe in Feld", Snackbar.LENGTH_LONG).show();
    }

    public void nutzer_delete(View view){
        collectionNutzerReference.document(currentNutzer.gibId()).delete();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NAME, "Kein Nutzer");
        editor.putString(ID, "");
        //editor.putInt("currentListPos", 0);
        editor.apply();
    }

    private boolean isNotEmpty(EditText view) {
        return view.getText().toString().trim().length() != 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String nutzer = crypt.decryptString(sharedPreferences.getString(SHARED_PREF_NAME, ""));
        menu.add(0, Menu.FIRST, Menu.NONE, nutzer + "   ").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}