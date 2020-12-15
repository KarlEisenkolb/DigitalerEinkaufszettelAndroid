package com.example.android.interaktivereinkaufszettel.Utility;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung;
import com.example.android.interaktivereinkaufszettel.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.FIRESTORE_NUTZER_COLLECTION;

public class CalculateGeldmanagmentAndSetMenu {

    final private static String TAG = "UtilityDebug";
    private CollectionReference collectionNutzerReference;
    private CollectionReference collectionIndividualBillReference;
    private String currentNutzerString;
    private Double kontostand;
    private Menu menu;
    private long categoryType;

    public CalculateGeldmanagmentAndSetMenu(String currentNutzer, Menu menuMain, Bundle categoryBundle){

        currentNutzerString = currentNutzer;
        menu = menuMain;

        categoryType = categoryBundle.getLong(Category.TYPE);
        String categoryID = categoryBundle.getString(Category.ID);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        collectionNutzerReference = firebaseFirestore.collection(FIRESTORE_NUTZER_COLLECTION);
        collectionIndividualBillReference = firebaseFirestore.collection(categoryID);

        if (categoryType == Category.CATEGORY_GROUP_LIST){
            collectionNutzerReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Double summeGehalt = 0.0;
                                Nutzer currentNutzer = null;
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    Nutzer docNutzer = doc.toObject(Nutzer.class);
                                    summeGehalt = summeGehalt + docNutzer.gibAnteil();
                                    if (docNutzer.gibName().equals(currentNutzerString))
                                        currentNutzer = docNutzer;
                                }
                                Double currentNutzerAnteil = currentNutzer.gibAnteil()/summeGehalt;
                                calculateCurrentGroupList(currentNutzerAnteil);
                                Log.d(TAG, "onComplete: "+currentNutzerAnteil);
                            }}
                    });
        }else
            calculateCurrentSoloList();
    }

    private void calculateCurrentGroupList(final Double currentNutzerAnteil){
        collectionIndividualBillReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            kontostand = 0.0;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Rechnung docRechnung = doc.toObject(Rechnung.class);
                                if (docRechnung.gibType() == Rechnung.RECHNUNG_GEKAUFT){
                                    if (docRechnung.gibKauefer().equals(currentNutzerString))
                                        kontostand = kontostand + docRechnung.gibPreis()*(1-currentNutzerAnteil);
                                    else
                                        kontostand = kontostand - docRechnung.gibPreis()*currentNutzerAnteil;
                                }
                                if (docRechnung.gibType() == Rechnung.RECHNUNG_ZAHLUNG){
                                    if (docRechnung.gibKauefer().equals(currentNutzerString))
                                        kontostand = kontostand + docRechnung.gibPreis();
                                    else
                                        kontostand = kontostand - docRechnung.gibPreis()*currentNutzerAnteil;
                                }
                            }
                            setMenu();
                        }}
                });
    }

    private void calculateCurrentSoloList(){
        collectionIndividualBillReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            kontostand = 0.0;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Rechnung docRechnung = doc.toObject(Rechnung.class);
                                if (docRechnung.gibType() != Rechnung.RECHNUNG_ZAHLUNG)
                                    kontostand = kontostand + docRechnung.gibPreis();
                            }
                            setMenu();
                        }}
                });
    }

    private void setMenu(){
        MenuItem menu_kontostand = menu.findItem(R.id.kontostand);
        menu_kontostand.setIcon(null);
        if (kontostand < -0.01) { // Rot wenn Kontostand weniger als -1 Cent
            SpannableString coloredString = new SpannableString(Math.round(kontostand * 100) / 100.0 + " €");
            coloredString.setSpan(new ForegroundColorSpan(Color.RED), 0, coloredString.length(), 0);
            menu_kontostand.setTitle(coloredString);
        }else
            menu_kontostand.setTitle(Math.round(kontostand * 100) / 100.0 + " €");
    }

}
