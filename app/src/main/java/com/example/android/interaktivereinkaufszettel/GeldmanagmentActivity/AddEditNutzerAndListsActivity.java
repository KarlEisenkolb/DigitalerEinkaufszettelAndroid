package com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.CategoryAdapter;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.NutzerAdapter;
import com.example.android.interaktivereinkaufszettel.Security.Crypt;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import com.example.android.interaktivereinkaufszettel.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import static com.example.android.interaktivereinkaufszettel.Security.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.Security.Crypt.CRYPT_USE_PASSPHRASE;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category.CATEGORY_GROUP_LIST;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category.CATEGORY_SOLO_LIST;
import static com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity.GeldmanagmentActivity.FIRESTORE_CATEGORY_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity.GeldmanagmentActivity.SHARED_PREF_NAME;
import static com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity.GeldmanagmentActivity.SHARED_PREF_NO_NUTZER;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer.ID;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer.NAME;
import static com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity.GeldmanagmentActivity.FIRESTORE_NUTZER_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity.GeldmanagmentActivity.SHARED_PREF;

public class AddEditNutzerAndListsActivity extends AppCompatActivity {

    private EditText nutzer_name_EditTextView;
    private EditText nutzer_gehalt_EditTextView;
    private EditText category_name_EditTextView;
    private Switch switchCategoryType;
    private NutzerAdapter adapterUser;
    private CategoryAdapter adapterCategory;
    private Nutzer currentClickedNutzer;
    private Category currentClickedCategory;
    private CollectionReference collectionNutzerReference;
    private CollectionReference collectionCategoryReference;
    private RecyclerView recyclerViewUser;
    private RecyclerView recyclerViewCategory;
    private FirebaseFirestore firebaseFirestore;
    private String currentNutzer;
    private TextView kategorie_owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_nutzer_and_lists);

        //String passphrase = getIntent().getExtras().getString(PASSPHRASE);
        //Crypt.initializePassphrase(passphrase);

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionNutzerReference = firebaseFirestore.collection(FIRESTORE_NUTZER_COLLECTION);
        collectionCategoryReference = firebaseFirestore.collection(FIRESTORE_CATEGORY_COLLECTION);

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String currentNutzerToCheck = sharedPreferences.getString(SHARED_PREF_NAME, SHARED_PREF_NO_NUTZER);

        if (!currentNutzerToCheck.equals(SHARED_PREF_NO_NUTZER)){
            Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
            currentNutzer = crypt.decryptString(currentNutzerToCheck);
        }else
            currentNutzer = currentNutzerToCheck;

        //===================================================================================================================================================
        //Kategorien Adapter
        //===================================================================================================================================================
        category_name_EditTextView = findViewById(R.id.kategorie_name);
        switchCategoryType = findViewById(R.id.kategorien_switch);
        kategorie_owner = findViewById(R.id.kategorie_owner);

        FirestoreRecyclerOptions<Category> optionsCategory = new FirestoreRecyclerOptions.Builder<Category>()
                .setQuery(collectionCategoryReference, Category.class)
                .build();

        recyclerViewCategory = findViewById(R.id.recycler_view_kategorien);
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategory.setNestedScrollingEnabled(false);
        adapterCategory = new CategoryAdapter(optionsCategory);
        recyclerViewCategory.setAdapter(adapterCategory);

        adapterCategory.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Category category) {
                category_name_EditTextView.setText(category.gibName());
                kategorie_owner.setText("Besitzer: "+category.gibBesitzer());
                if (category.gibType() == CATEGORY_SOLO_LIST)
                    switchCategoryType.setChecked(true);
                else
                    switchCategoryType.setChecked(false);
                currentClickedCategory = category;
            }
        });

        //===================================================================================================================================================
        //Nutzer Einstellungen Adapter
        //===================================================================================================================================================
        nutzer_name_EditTextView = findViewById(R.id.nutzer_name);
        nutzer_gehalt_EditTextView = findViewById(R.id.nutzer_gehalt);

        FirestoreRecyclerOptions<Nutzer> optionsUser = new FirestoreRecyclerOptions.Builder<Nutzer>()
                .setQuery(collectionNutzerReference, Nutzer.class)
                .build();

        recyclerViewUser = findViewById(R.id.recycler_view_nutzer);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setNestedScrollingEnabled(false);
        adapterUser = new NutzerAdapter(optionsUser);
        recyclerViewUser.setAdapter(adapterUser);

        adapterUser.setOnItemClickListener(new NutzerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nutzer nutzer) {
                nutzer_name_EditTextView.setText(nutzer.gibName());
                nutzer_gehalt_EditTextView.setText(String.valueOf(nutzer.gibZahlungsanteil()));
                currentClickedNutzer = nutzer;
            }
        });
    }

    //===================================================================================================================================================
    //Nutzer Buttons
    //===================================================================================================================================================
    public void nutzer_add(View view){
        if (isNotEmpty(nutzer_name_EditTextView) && isNotEmpty(nutzer_gehalt_EditTextView) && adapterUser.getSnapshots().size() < 2) {
            String nutzername =  nutzer_name_EditTextView.getText().toString();
            Double gehalt = Double.valueOf(nutzer_gehalt_EditTextView.getText().toString());
            DocumentReference docRef = collectionNutzerReference.document();
            docRef.set(new Nutzer(nutzername, gehalt, docRef.getId()));
        } else
            Snackbar.make(recyclerViewUser, "Leere Eingabe oder mehr als 2 Nutzer", Snackbar.LENGTH_LONG).show();
    }

    public void nutzer_update(View view){
        if (isNotEmpty(nutzer_name_EditTextView) && isNotEmpty(nutzer_gehalt_EditTextView)) {
            Crypt cryptNormal = new Crypt(CRYPT_USE_DEFAULT_KEY);
            Crypt cryptPassphrase = new Crypt(CRYPT_USE_PASSPHRASE);

            WriteBatch batch = firebaseFirestore.batch();
            batch.update(collectionNutzerReference.document(currentClickedNutzer.gibId()), Nutzer.NAME, cryptNormal.encryptString(nutzer_name_EditTextView.getText().toString()));
            batch.update(collectionNutzerReference.document(currentClickedNutzer.gibId()), Nutzer.ZAHLUNGSANTEIL, cryptPassphrase.encryptDouble(Double.valueOf(nutzer_gehalt_EditTextView.getText().toString())));
            batch.commit();
        } else
            Snackbar.make(recyclerViewUser, "Leere Eingabe in Feld", Snackbar.LENGTH_LONG).show();
    }

    public void nutzer_delete(View view){
        collectionNutzerReference.document(currentClickedNutzer.gibId()).delete();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NAME, "Kein Nutzer");
        editor.putString(ID, "");
        //editor.putInt("currentListPos", 0);
        editor.apply();
    }

    //===================================================================================================================================================
    //Kategorien Buttons
    //===================================================================================================================================================
    public void category_add(View view){
        if (isNotEmpty(category_name_EditTextView)) {
            String categoryname =  category_name_EditTextView.getText().toString();
            long type = CATEGORY_GROUP_LIST;
            if (switchCategoryType.isChecked())
                type = CATEGORY_SOLO_LIST;

            DocumentReference docRef = collectionCategoryReference.document();
            docRef.set(new Category(categoryname, type, currentNutzer, docRef.getId()));
        } else
            Snackbar.make(recyclerViewCategory, "Leere Eingabe in Feld", Snackbar.LENGTH_LONG).show();
    }

    public void category_update(View view){
        if (isNotEmpty(category_name_EditTextView)) {
            Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);

            WriteBatch batch = firebaseFirestore.batch();
            long type = CATEGORY_GROUP_LIST;
            if (switchCategoryType.isChecked())
                type = CATEGORY_SOLO_LIST;

            batch.update(collectionCategoryReference.document(currentClickedCategory.gibId()), Category.NAME, crypt.encryptString(category_name_EditTextView.getText().toString()));
            batch.update(collectionCategoryReference.document(currentClickedCategory.gibId()), Category.TYPE, crypt.encryptLong(type));
            batch.commit();
        } else
            Snackbar.make(recyclerViewUser, "Leere Eingabe in Feld", Snackbar.LENGTH_LONG).show();
    }

    public void category_delete(View view){
        collectionCategoryReference.document(currentClickedCategory.gibId()).delete();
    }

    //===================================================================================================================================================
    //Weitere Funktionen
    //===================================================================================================================================================
    private boolean isNotEmpty(EditText view) {
        return view.getText().toString().trim().length() != 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String currentNutzer = sharedPreferences.getString(SHARED_PREF_NAME, SHARED_PREF_NO_NUTZER);

        if (!currentNutzer.equals(SHARED_PREF_NO_NUTZER)){
            menu.add(0, Menu.FIRST, Menu.NONE, crypt.decryptString(currentNutzer) + "   ").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }else
            menu.add(0, Menu.FIRST, Menu.NONE, currentNutzer + "   ").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterUser.startListening();
        adapterCategory.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterUser.stopListening();
        adapterCategory.stopListening();
    }
}