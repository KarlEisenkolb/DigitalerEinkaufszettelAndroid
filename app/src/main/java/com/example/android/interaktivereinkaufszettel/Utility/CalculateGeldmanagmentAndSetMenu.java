package com.example.android.interaktivereinkaufszettel.Utility;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity.PlaceholderFragment;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung;
import com.example.android.interaktivereinkaufszettel.R;
import com.example.android.interaktivereinkaufszettel.Security.Crypt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity.GeldmanagmentActivity.NUMBER_OF_RECHNUNGEN_LOADED_PER_ADAPTER;

public class CalculateGeldmanagmentAndSetMenu {

    private CollectionReference collectionIndividualBillReference;
    private String currentNutzerString;
    private Menu menu;
    private static OnCalculationDoneListener listener;
    private PlaceholderFragment currentFragment;
    private int position;
    private String groupOrSoloList;
    private long adapterCategory;

    private long timestampOfFirstRechnung;
    private Double kontostand;
    private Double gesamtAusgaben;
    private int gesamtBelegeCount;
    private Double monatAusgaben;

    boolean notTriggeredOnce;
    boolean lastDocWasSummary;
    int currentYear;
    Month currentMonth;
    DocumentReference currentMonthSummaryRef;
    double currentMonthSummaryPreis;
    Rechnung docRechnung;
    long docRechnungType;
    ZonedDateTime docDatum;

    final private long MONTH_MILLIS = 31556952L / 12 * 1000;

    public CalculateGeldmanagmentAndSetMenu(final String currentNutzer, final Menu menuMain, final Category adapterCategory, final PlaceholderFragment currentFragment){
        this.currentNutzerString = currentNutzer;
        this.menu = menuMain;
        this.collectionIndividualBillReference = FirebaseFirestore.getInstance().collection(adapterCategory.gibId());
        this.position = currentFragment.getPosition();
        this.currentFragment = currentFragment;
        this.adapterCategory = adapterCategory.gibType();

        if (this.adapterCategory == Category.CATEGORY_GROUP_LIST)
            groupOrSoloList = "Gruppe";
        else
            groupOrSoloList = "Solo";

        new Thread(new Runnable() {
            @Override
            public void run() {
                calculateList();
            }
        }).start();
    }

    public void calculateList(){
        collectionIndividualBillReference
                .orderBy(Rechnung.DATUM, Query.Direction.DESCENDING)
                .limit(NUMBER_OF_RECHNUNGEN_LOADED_PER_ADAPTER + 1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            variableSetup();

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                docRechnung = doc.toObject(Rechnung.class);
                                docRechnungType = docRechnung.gibType();
                                Log.d("CalculationLogger", docRechnung.toString());

                                docDatum = ZonedDateTime.ofInstant(Instant.ofEpochMilli(docRechnung.gibDatum()), ZoneId.systemDefault());
                                if (notTriggeredOnce){
                                    firstTrigger(task);
                                    notTriggeredOnce = false;
                                }

                                if(currentMonth != docDatum.getMonth() || currentYear != docDatum.getYear()){
                                    if(currentDocumentHasDifferentMonth())
                                        return;
                                }

                                if(docRechnungType != Rechnung.MONTH_SUMMARY) {
                                    gesamtAusgaben += docRechnung.gibPreis();
                                    gesamtBelegeCount++;
                                    monatAusgaben += docRechnung.gibPreis();
                                    lastDocWasSummary = false;
                                    if (adapterCategory == Category.CATEGORY_GROUP_LIST)
                                        calculateKonto();
                                    else
                                        calculateSoloKonto();
                                }
                            }
                            updateLastMonthSummaryAfterLoop(task);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    setMenu();
                                    double trackedMonths = (System.currentTimeMillis() - timestampOfFirstRechnung)/MONTH_MILLIS;
                                    listener.setMenuTextViewsOfFragment(groupOrSoloList, Math.round(gesamtAusgaben/trackedMonths * 100) / 100.0, gesamtBelegeCount, position);
                                }
                            });
                        }}
                });
    }

    private void firstTrigger(@NonNull Task<QuerySnapshot> task) {
        currentMonth = docDatum.getMonth();
        currentYear = docDatum.getYear();
        if (docRechnungType == Rechnung.MONTH_SUMMARY){
            currentMonthSummaryRef = collectionIndividualBillReference.document(docRechnung.gibId()); // Summary für diesen Monat gefunden und gespeichert. In der nächsten Iteration wird der erste Beleg des neuen Monats bearbeitet.
            currentMonthSummaryPreis = docRechnung.gibPreis();
            lastDocWasSummary = true;
            if (task.getResult().size() == 1)
                collectionIndividualBillReference.document(docRechnung.gibId()).delete(); // falls erstes Dokument Summary und einzigtes Dokument ist.. löschen.. leerer Monat
        }
    }

    private void updateLastMonthSummaryAfterLoop(@NonNull Task<QuerySnapshot> task) {
        if(currentMonthSummaryRef == null && task.getResult().size() != 0) {
            addSummaryToLatestMonthIfNeeded();
        }else if(task.getResult().size() < NUMBER_OF_RECHNUNGEN_LOADED_PER_ADAPTER + 1 && docRechnungType == Rechnung.MONTH_SUMMARY){
            currentMonthSummaryRef.delete();
        }else if(task.getResult().size() != 0){
            updateLatestMonthSummaryIfNeeded(task);
        }
    }

    private void updateLatestMonthSummaryIfNeeded(@NonNull Task<QuerySnapshot> task) {
        Crypt crypt = new Crypt(Crypt.CRYPT_USE_DEFAULT_KEY);
        if(currentMonthSummaryPreis != monatAusgaben && (task.getResult().size() < NUMBER_OF_RECHNUNGEN_LOADED_PER_ADAPTER + 1 || docRechnungType != Rechnung.MONTH_SUMMARY)) {
            currentMonthSummaryRef.update(Rechnung.PREIS, crypt.encryptDouble(monatAusgaben));
        }
    }

    private void addSummaryToLatestMonthIfNeeded() {
        currentMonthSummaryRef = collectionIndividualBillReference.document();
        ZonedDateTime currentMonthObject = ZonedDateTime.of(currentYear, currentMonth.getValue(), 1, 0, 0, 0, 0, ZoneId.systemDefault()).plus(1, ChronoUnit.MONTHS);
        currentMonthSummaryRef.set(Rechnung.addMonthSummary(monatAusgaben, currentMonthObject.toInstant().toEpochMilli()-1, currentMonthSummaryRef.getId()));
    }

    private void variableSetup() {
        timestampOfFirstRechnung = System.currentTimeMillis();
        kontostand = 0.0;
        gesamtAusgaben = 0.0;
        gesamtBelegeCount = 0;
        monatAusgaben = 0.0;

        notTriggeredOnce = true;
        lastDocWasSummary = false; // falls dieser Trigger anschlägt sind 2 Summary's hintereinander daher der Monat hat keine Items mehr
        currentYear = ZonedDateTime.now().getYear();
        currentMonth = ZonedDateTime.now().getMonth();
        currentMonthSummaryRef = null;
        currentMonthSummaryPreis = 0.0;
        docRechnung = null;
    }

    private void calculateKonto() {
        if (docRechnung.gibDatum() < timestampOfFirstRechnung)
            timestampOfFirstRechnung = docRechnung.gibDatum();
        Double currentNutzerAnteil = extractNutzerAnteil(docRechnung, currentNutzerString);
        if (docRechnung.gibType() == Rechnung.RECHNUNG_GEKAUFT) {
            if (docRechnung.gibKauefer().equals(currentNutzerString))
                kontostand += docRechnung.gibPreis() * (1 - currentNutzerAnteil);
            else
                kontostand -= docRechnung.gibPreis() * currentNutzerAnteil;
        }
        if (docRechnungType == Rechnung.RECHNUNG_ZAHLUNG) {
            if (docRechnung.gibKauefer().equals(currentNutzerString))
                kontostand += docRechnung.gibPreis();
            else
                kontostand -= docRechnung.gibPreis() * currentNutzerAnteil;
        }
    }

    private void calculateSoloKonto() {
        if(docRechnung.gibDatum() < timestampOfFirstRechnung)
            timestampOfFirstRechnung = docRechnung.gibDatum();
        if (docRechnung.gibType() != Rechnung.RECHNUNG_ZAHLUNG)
            kontostand += docRechnung.gibPreis();
    }

    private boolean currentDocumentHasDifferentMonth() {
        ZonedDateTime currentMonthObject = ZonedDateTime.of(currentYear, currentMonth.getValue(), 1, 0, 0, 0, 0, ZoneId.systemDefault()).plus(1, ChronoUnit.MONTHS);

        if(currentMonthSummaryRef == null) { // neuer Monat und erster Beleg des neuen Monats und noch KEINE Summary des letzten vorhanden
            currentMonthSummaryRef = collectionIndividualBillReference.document();
            currentMonthSummaryRef.set(Rechnung.addMonthSummary(monatAusgaben, currentMonthObject.toInstant().toEpochMilli() - 1, currentMonthSummaryRef.getId()));
        }else if(lastDocWasSummary && docRechnungType == Rechnung.MONTH_SUMMARY) { // zwei Summary's folgen aufeinander dann lösche das aktuelle Dokument da leerer Monat vorhanden
            currentMonthSummaryRef.delete();
            calculateList();
            return true;
        }else if(currentMonthSummaryPreis != monatAusgaben){ //neuer Monat und erster Beleg des neuen Monats, Summary des letzten ist vorhanden und Check ob dieser geupdated werden muss
            Crypt crypt = new Crypt(Crypt.CRYPT_USE_DEFAULT_KEY);
            currentMonthSummaryRef.update(Rechnung.PREIS, crypt.encryptDouble(monatAusgaben));
        }

        currentMonth = docDatum.getMonth();
        currentYear = docDatum.getYear();
        currentMonthSummaryRef = null;
        currentMonthSummaryPreis = 0.0;
        monatAusgaben = 0.0;

        if(docRechnung.gibType() == Rechnung.MONTH_SUMMARY) {
            currentMonthSummaryRef = collectionIndividualBillReference.document(docRechnung.gibId()); // Summary für diesen Monat gefunden und gespeichert. In der nächsten Iteration wird der erste Beleg des neuen Monats bearbeitet.
            currentMonthSummaryPreis = docRechnung.gibPreis();
            lastDocWasSummary = true;
        }
        return false;
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

    public static void setOnCalculationDoneListener(OnCalculationDoneListener setListener){
        listener = setListener;
    }

    public interface OnCalculationDoneListener{
        void setMenuTextViewsOfFragment(String groupOrSoloList, double averagePerMonth, int itemcount, int fragmentposition);
    }
}
