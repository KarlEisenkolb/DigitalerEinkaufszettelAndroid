package com.example.android.interaktivereinkaufszettel.Utility;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import com.example.android.interaktivereinkaufszettel.Geldmanagment.PlaceholderFragment;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung;
import com.example.android.interaktivereinkaufszettel.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class CalculateGeldmanagmentAndSetMenu {

    private CollectionReference collectionIndividualBillReference;
    private String currentNutzerString;
    private Double kontostand;
    private Menu menu;

    public CalculateGeldmanagmentAndSetMenu(String currentNutzer, Menu menuMain, Category adapterCategory, PlaceholderFragment currentFragment){
        currentNutzerString = currentNutzer;
        menu = menuMain;
        collectionIndividualBillReference = FirebaseFirestore.getInstance().collection(adapterCategory.gibId());

        if (adapterCategory.gibType() == Category.CATEGORY_GROUP_LIST){
            calculateCurrentGroupList();
        }else
            calculateCurrentSoloList();
    }

    public void calculateCurrentGroupList(){
        collectionIndividualBillReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            kontostand = 0.0;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Rechnung docRechnung = doc.toObject(Rechnung.class);
                                Double currentNutzerAnteil = extractNutzerAnteil(docRechnung, currentNutzerString);
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

    public static Double extractNutzerAnteil(Rechnung rechnung, String currentNutzerString) {
        Double gesamtAnteile = 0.0;
        Double nutzerAnteil = 0.0;
        List<String> nutzer     = rechnung.gibNutzerListe();
        List<Double> anteile    = rechnung.gibNutzerZahlungsanteile();

        for (int i=0; i<nutzer.size(); i++){
            gesamtAnteile += anteile.get(i);
            if (nutzer.get(i).equals(currentNutzerString))
                nutzerAnteil = anteile.get(i);
        }
        return nutzerAnteil/gesamtAnteile;
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
