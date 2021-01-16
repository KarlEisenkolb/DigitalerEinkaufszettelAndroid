package com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.example.android.interaktivereinkaufszettel.Dialogs.NewRechnungDialog;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer;
import com.example.android.interaktivereinkaufszettel.Security.Crypt;
import com.example.android.interaktivereinkaufszettel.Security.CustomFingerprintSecurityHandling;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import com.example.android.interaktivereinkaufszettel.R;
import com.example.android.interaktivereinkaufszettel.Utility.CalculateGeldmanagmentAndSetMenu;
import com.example.android.interaktivereinkaufszettel.Utility.CustomGlobalContext;
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
import static com.example.android.interaktivereinkaufszettel.Security.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category.CATEGORY_GROUP_LIST;

public class GeldmanagmentActivity extends AppCompatActivity {

    final static public int NUMBER_OF_RECHNUNGEN_LOADED_PER_ADAPTER         = 40;
    final static public String FIRESTORE_NUTZER_COLLECTION                  = "cLhew80dDbSjs0bs3m7dM8";
    final static public String FIRESTORE_CATEGORY_COLLECTION                = "lGwp4B9sJNsU8M1Dp9B5sI"; // Liste an CategoryId's
    final static public String FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION     = "p0WhvEhpE93RGct0peCj";
    final static public String FIRESTORE_EINKAUFSZETTEL_CATEGORY_NAME       = "Haushalt";

    public static final String SHARED_PREF = "shared_pref";
    public static final String SHARED_PREF_NAME = "name";
    public static final String SHARED_PREF_STANDARD_EINKAUFSNAME = "Einkauf";
    public static final String SHARED_PREF_NO_NUTZER = "Kein Nutzer";
    final static String PASSPHRASE = "passphrase";

    private final List<Category> categories = new ArrayList<>();
    private TabLayout tabs;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private CollectionReference collectionCategoryReference;
    private String currentNutzer;
    private Menu menu;
    private CustomGlobalContext cgc = CustomGlobalContext.getInstance();
    private TextView averageView;
    private TextView countView;
    private TextView groupView;

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

        collectionCategoryReference = FirebaseFirestore.getInstance().collection(FIRESTORE_CATEGORY_COLLECTION);

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String currentNutzerToCheck = sharedPreferences.getString(SHARED_PREF_NAME, SHARED_PREF_NO_NUTZER);

        if (!currentNutzerToCheck.equals(SHARED_PREF_NO_NUTZER)){
            Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
            currentNutzer = crypt.decryptString(currentNutzerToCheck);
        }else
            currentNutzer = currentNutzerToCheck;

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        averageView = findViewById(R.id.average_monthly);
        countView = findViewById(R.id.rechnungen_count);
        groupView = findViewById(R.id.solo_or_grouplist);

        final FloatingActionButton fabBill = findViewById(R.id.fab_add_bill);
        final FloatingActionButton fabNutzer = findViewById(R.id.fab_choose_nutzer);

        CalculateGeldmanagmentAndSetMenu.setOnCalculationDoneListener(new CalculateGeldmanagmentAndSetMenu.OnCalculationDoneListener() {
            @Override
            public void setMenuTextViewsOfFragment(String groupOrSoloList, double averagePerMonth, int itemcount, int fragmentposition) {
                    groupView.setText(groupOrSoloList);
                    countView.setText(itemcount + " Belege");
                    if (averagePerMonth < 10000)
                        averageView.setText(averagePerMonth + " €/Monat");
                    else
                        averageView.setText("no Average");
            }
        });

        fabBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewRechnungDialog rechnungDialog = NewRechnungDialog.newAddInstance(getCurrentFragment().getCategory(), currentNutzer, new NewRechnungDialog.OnDialogFinishedListener() {
                    @Override
                    public void onDialogFinished() {
                        new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, getCurrentFragment().getCategory(), getCurrentFragment());
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
                    PopupMenu popup = new PopupMenu(GeldmanagmentActivity.this, fabNutzer);
                    int i = 0;
                    for (Nutzer currentNutzer : cgc.getNutzerList()) {
                        popup.getMenu().add(NONE, i, NONE, currentNutzer.gibName());
                        i++;
                    }

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            currentNutzer = item.getTitle().toString();
                            Snackbar.make(fabNutzer, currentNutzer, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();

                            Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(SHARED_PREF_NAME, crypt.encryptString(item.getTitle().toString()));
                            editor.apply();

                            cgc.firestoreUpdateNutzerListAndSetMenu(item.getTitle().toString(), menu);
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

    private PlaceholderFragment getCurrentFragment() {
        return sectionsPagerAdapter.getItem(viewPager.getCurrentItem());
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
                        sectionsPagerAdapter = new SectionsPagerAdapter(categories, getSupportFragmentManager());
                        viewPager.setAdapter(sectionsPagerAdapter);
                        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                            }

                            @Override
                            public void onPageSelected(int position) {
                                new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, sectionsPagerAdapter.getItem(position).getCategory(), sectionsPagerAdapter.getItem(position));
                                Log.d("PageChangerLogging", "onPageSelected: " + position);
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        });
                        tabs.setupWithViewPager(viewPager);
                        if (menu != null){
                           new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, getCurrentFragment().getCategory(), getCurrentFragment());
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

        if(!currentNutzer.equals(SHARED_PREF_NO_NUTZER)){
            Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
            cgc.firestoreUpdateNutzerListAndSetMenu(crypt.decryptString(currentNutzer), menu);
        }else
            CustomGlobalContext.getInstance().firestoreUpdateNutzerListAndSetMenu(currentNutzer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.kontostand:
                new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, getCurrentFragment().getCategory(), getCurrentFragment());
                return true;
            case R.id.print:
                return true;
            case R.id.nutzerAndLists:
                new CustomFingerprintSecurityHandling(GeldmanagmentActivity.this, new CustomFingerprintSecurityHandling.FingerprintSuccessListener() {
                    @Override
                    public void onFingerprintSuccess() {
                        Intent intent = new Intent(GeldmanagmentActivity.this, AddEditNutzerAndListsActivity.class);
                        startActivity(intent);
                    }
                });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}