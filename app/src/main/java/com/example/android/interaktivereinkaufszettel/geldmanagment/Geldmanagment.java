package com.example.android.interaktivereinkaufszettel.geldmanagment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.example.android.interaktivereinkaufszettel.Crypt;
import com.example.android.interaktivereinkaufszettel.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.interaktivereinkaufszettel.geldmanagment.ui.main.SectionsPagerAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.view.Menu.NONE;
import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;

public class Geldmanagment extends AppCompatActivity {

    final static String FIRESTORE_NUTZER_COLLECTION       = "cLhew80dDbSjs0bs3m7dM8";
    final static String FIRESTORE_BILLS_COLLECTION        = "nQ9B2j5BsEui5svSLme3s2";
    final static String FIRESTORE_CATEGORY_COLLECTION     = "lGwp4B9sJNsU8M1Dp9B5sI";

    public static final String SHARED_PREF = "shared_pref";
    public static final String SHARED_PREF_NAME = "name";
    public static final String SHARED_PREF_NO_NUTZER = "Kein Nutzer";
    final static String PASSPHRASE = "passphrase";

    private List<Nutzer> nutzerList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private String currentNutzer;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geldmanagment);
        Toolbar toolbar = findViewById(R.id.toolbar_geldmanagment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionNutzerReference = firebaseFirestore.collection(FIRESTORE_NUTZER_COLLECTION);

        collectionNutzerReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            nutzerList.clear();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                nutzerList.add(doc.toObject(Nutzer.class));
                            }
                        }}});

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        final FloatingActionButton fabBill = findViewById(R.id.fab_add_bill);
        final FloatingActionButton fabNutzer = findViewById(R.id.fab_choose_nutzer);

        fabBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fabNutzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInternetConnection(getApplicationContext())){
                    PopupMenu popup = new PopupMenu(Geldmanagment.this, fabNutzer);
                    int i = 0;
                    for (Nutzer currentNutzer : nutzerList) {
                        popup.getMenu().add(NONE, i, NONE, currentNutzer.gibName());
                        i++;
                    }

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            Snackbar.make(fabNutzer, item.getTitle(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();

                            Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(SHARED_PREF_NAME, crypt.encryptString(item.getTitle().toString()));
                            editor.apply();

                            MenuItem current_user = menu.findItem(R.id.current_user);
                            current_user.setTitle(item.getTitle().toString());
                            currentNutzer = item.getTitle().toString();
                            //INDIVIDUAL_TRAINING_REFERENCE = firebaseFirestore.collection(DOC_REFERENCE_TRAINING + item.getTitle().toString());
                            return true;
                        }
                    });
                    popup.show();
                }else{
                    Toast.makeText(getApplicationContext(), "Nicht verfügbar, weil Offline", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static boolean checkInternetConnection(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_geldmanagment, menu);
        this.menu = menu;

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String currentNutzer = sharedPreferences.getString(SHARED_PREF_NAME, SHARED_PREF_NO_NUTZER);

        MenuItem nutzer = menu.findItem(R.id.current_user);
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        nutzer.setTitle(crypt.decryptString(currentNutzer));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.print:
                //startActivity(new Intent(MainActivity.this, StatistikenActivity.class));
                return true;
            case R.id.nutzerAndLists:
                PassphrasenDialog passphrasenDialog = PassphrasenDialog.newInstance(Geldmanagment.this, new PassphrasenDialog.OnDialogFinishedListener() {
                    @Override
                    public void onDialogFinished(String passphrase) {
                        Intent intent = new Intent(Geldmanagment.this,AddEditNutzerAndLists.class);
                        intent.putExtra(PASSPHRASE, passphrase);
                        startActivity(intent);
                    }
                });
                passphrasenDialog.show(getSupportFragmentManager(), "PassphrasenDialog");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}