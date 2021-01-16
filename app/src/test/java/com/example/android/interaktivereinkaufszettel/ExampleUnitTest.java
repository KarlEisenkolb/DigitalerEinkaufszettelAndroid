package com.example.android.interaktivereinkaufszettel;

import android.content.SharedPreferences;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung;
import com.example.android.interaktivereinkaufszettel.Utility.CustomGlobalContext;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.FIRESTORE_EINKAUFSZETTEL_CATEGORY_NAME;
import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.FIRESTORE_NUTZER_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.SHARED_PREF_NO_NUTZER;
import static com.example.android.interaktivereinkaufszettel.Geldmanagment.Geldmanagment.SHARED_PREF_STANDARD_EINKAUFSNAME;
import static com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung.RECHNUNG_GEKAUFT;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
}