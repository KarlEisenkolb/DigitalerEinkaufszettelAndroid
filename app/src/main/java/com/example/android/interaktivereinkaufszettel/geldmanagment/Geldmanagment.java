package com.example.android.interaktivereinkaufszettel.geldmanagment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.example.android.interaktivereinkaufszettel.Crypt;
import com.example.android.interaktivereinkaufszettel.CustomFingerprintSecurityHandling;
import com.example.android.interaktivereinkaufszettel.R;
import com.example.android.interaktivereinkaufszettel.geldmanagment.ui.main.SectionsPagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.view.Menu.NONE;
import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Category.CATEGORY_GROUP_LIST;

public class Geldmanagment extends AppCompatActivity {

    final static public String FIRESTORE_NUTZER_COLLECTION                  = "cLhew80dDbSjs0bs3m7dM8";
    final static public String FIRESTORE_CATEGORY_COLLECTION                = "lGwp4B9sJNsU8M1Dp9B5sI";
    final static public String FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION     = "p0WhvEhpE93RGct0peCj";
    final static public String FIRESTORE_EINKAUFSZETTEL_CATEGORY_NAME       = "Haushalt";

    public static final String SHARED_PREF = "shared_pref";
    public static final String SHARED_PREF_NAME = "name";
    public static final String SHARED_PREF_STANDARD_EINKAUFSNAME = "Einkauf";
    public static final String SHARED_PREF_NO_NUTZER = "Kein Nutzer";
    final static String PASSPHRASE = "passphrase";

    private final List<Category> categories = new ArrayList<>();
    private TabLayout tabs;
    public static ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private CollectionReference collectionNutzerReference;
    private CollectionReference collectionCategoryReference;
    private List<Nutzer> nutzerList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    public static String currentNutzer;
    public static Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geldmanagment);
        Toolbar toolbar = findViewById(R.id.toolbar_geldmanagment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(PASSPHRASE)) {
            String passphrase = getIntent().getExtras().getString(PASSPHRASE);
            Crypt.initializePassphrase(passphrase);
        }

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

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        final FloatingActionButton fabBill = findViewById(R.id.fab_add_bill);
        final FloatingActionButton fabNutzer = findViewById(R.id.fab_choose_nutzer);

        fabBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewRechnungDialog rechnungDialog = NewRechnungDialog.newAddInstance(sectionsPagerAdapter.getItem(viewPager.getCurrentItem()).getArguments(), currentNutzer, new NewRechnungDialog.OnDialogFinishedListener() {
                    @Override
                    public void onDialogFinished() {
                        new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, sectionsPagerAdapter.getItem(viewPager.getCurrentItem()).getArguments());
                    }
                });
                rechnungDialog.show(getSupportFragmentManager(), "RechnungDialog");
            }
        });

        settingPagingSections();

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
                            settingPagingSections();
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

    private void settingPagingSections(){
        if (currentNutzer.equals(SHARED_PREF_NO_NUTZER))
            Toast.makeText(this, "Nutzer auswählen oder erstellen...", Toast.LENGTH_LONG).show();
        else {
            categories.clear();
            Category einkaufszettel = new Category(FIRESTORE_EINKAUFSZETTEL_CATEGORY_NAME, CATEGORY_GROUP_LIST, currentNutzer, FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION);
            categories.add(einkaufszettel);
            collectionCategoryReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Category category = doc.toObject(Category.class);
                            if (category.gibType() == CATEGORY_GROUP_LIST || currentNutzer.equals(category.gibBesitzer())) {
                                categories.add(category);
                            }
                        }
                        sectionsPagerAdapter = new SectionsPagerAdapter(Geldmanagment.this, categories, getSupportFragmentManager());
                        viewPager.setAdapter(sectionsPagerAdapter);
                        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                            }

                            @Override
                            public void onPageSelected(int position) {
                                new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, sectionsPagerAdapter.getItem(position).getArguments());
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        });
                        tabs.setupWithViewPager(viewPager);
                        if (menu != null){
                           new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, sectionsPagerAdapter.getItem(viewPager.getCurrentItem()).getArguments());
                        }
                    }
                }
            });
        }
    }

    public static boolean checkInternetConnection(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_geldmanagment, menu);
        this.menu = menu;

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String currentNutzer = sharedPreferences.getString(SHARED_PREF_NAME, SHARED_PREF_NO_NUTZER);

        MenuItem nutzer = menu.findItem(R.id.current_user);
        if (!currentNutzer.equals(SHARED_PREF_NO_NUTZER)){
            Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
            nutzer.setTitle(crypt.decryptString(currentNutzer));
        }else
            nutzer.setTitle(currentNutzer);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.kontostand:
                new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, sectionsPagerAdapter.getItem(viewPager.getCurrentItem()).getArguments());
                return true;
            case R.id.print:
                return true;
            case R.id.nutzerAndLists:
                new CustomFingerprintSecurityHandling(Geldmanagment.this, new CustomFingerprintSecurityHandling.FingerprintSuccessListener() {
                    @Override
                    public void onFingerprintSuccess() {
                        Intent intent = new Intent(Geldmanagment.this,AddEditNutzerAndLists.class);
                        startActivity(intent);
                    }
                });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}