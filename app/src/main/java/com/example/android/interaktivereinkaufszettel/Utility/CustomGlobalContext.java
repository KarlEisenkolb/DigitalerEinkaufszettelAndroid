package com.example.android.interaktivereinkaufszettel.Utility;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer;
import com.example.android.interaktivereinkaufszettel.R;
import com.example.android.interaktivereinkaufszettel.Security.Crypt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.FIRESTORE_NUTZER_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.SHARED_PREF_NO_NUTZER;
import static com.example.android.interaktivereinkaufszettel.Security.Crypt.CRYPT_USE_DEFAULT_KEY;

public class CustomGlobalContext {

    private static CustomGlobalContext customGlobalContext;
    private List<Nutzer> nutzerList = new ArrayList<>();
    private String currentNutzer;
    private Menu menu;

    public static CustomGlobalContext getInstance(){
        if (customGlobalContext == null)
            customGlobalContext = new CustomGlobalContext();
        return customGlobalContext;
    }

    public String getCurrentNutzer() { return currentNutzer; }
    public Menu getGeldmanagmentMenu() {
        return menu;
    }

    public void firestoreUpdateNutzerListAndSetMenu(final String currentNutzer, final Menu menu){
        this.currentNutzer = currentNutzer;
        this.menu = menu;
        FirebaseFirestore.getInstance().collection(FIRESTORE_NUTZER_COLLECTION).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            nutzerList.clear();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                nutzerList.add(doc.toObject(Nutzer.class));
                            }
                            MenuItem nutzer = menu.findItem(R.id.current_user);
                            if (!currentNutzer.equals(SHARED_PREF_NO_NUTZER)){
                                nutzer.setTitle(currentNutzer + " (" + Math.round(extractNutzerAnteil(currentNutzer) * 10000) / 100.0 + "%)");
                            }else
                                nutzer.setTitle(currentNutzer);
                        }
                    }});
    }

    private static Double extractNutzerAnteil(String currentNutzer){
        Double gesamtAnteile = 0.0;
        Double nutzerAnteil = 0.0;
        for (Nutzer nutzer : CustomGlobalContext.getInstance().getNutzerList()){
            gesamtAnteile += nutzer.gibZahlungsanteil();
            if (nutzer.gibName().equals(currentNutzer))
                nutzerAnteil = nutzer.gibZahlungsanteil();
        }
        return nutzerAnteil/gesamtAnteile;
    }

    public List<Nutzer> getNutzerList(){
        return this.nutzerList;
    }
}
