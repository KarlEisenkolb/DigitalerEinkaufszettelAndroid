package com.example.android.interaktivereinkaufszettel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment;
import com.example.android.interaktivereinkaufszettel.geldmanagment.PassphrasenDialog;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.CustomFingerprintSecurityHandling.PASSPHRASE;
import static com.example.android.interaktivereinkaufszettel.Note.ADAPTER_POS;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.SHARED_PREF;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.SHARED_PREF_NAME;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.SHARED_PREF_NO_NUTZER;

public class MainActivity extends AppCompatActivity {

    final static String FIRESTORE_EINKAUFSZETTEL_COLLECTION         = "xgYkyHsoUIF33PIk11xWvM";
    final static String FIRESTORE_SAVE_EINKAUFSZETTEL_COLLECTION    = "xW5LGz1vR9D67rIwH9yROQ";
    final static int RC_SIGN_IN = 0;

    static String which_category = "default";
    private CustomFirebaseSecurityHandling securityHandling;
    private AudioManager audio;
    private SoundPool soundPool;
    private int deleteSound, finishSound, turn_orangeSound, turn_greenSound, undoSound;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private CollectionReference collectionSaveReference;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private Menu menu;
    private FloatingActionButton fab_done;
    private Crypt crypt;
    private String[] categoryNameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        securityHandling = new CustomFirebaseSecurityHandling(this);

        final CustomSpeechRecognition customSpeechRecognition = new CustomSpeechRecognition(this);

        //===================================================================================================================================================
        //Audio Setup
        //===================================================================================================================================================
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        deleteSound = soundPool.load(this, R.raw.delete, 1);
        finishSound = soundPool.load(this, R.raw.finish, 1);
        turn_orangeSound = soundPool.load(this, R.raw.turn_orange, 1);
        turn_greenSound = soundPool.load(this, R.raw.turn_green, 1);
        undoSound = soundPool.load(this, R.raw.undo, 1);

        //===================================================================================================================================================
        //Hardcoded Kategorien festlegen
        //===================================================================================================================================================
        final int category_count = 7;
        String[] categoryNameArray = new String[category_count];
        categoryNameArray[0] = "Sonstiges";
        categoryNameArray[1] = "Drogerieprodukte";
        categoryNameArray[2] = "Milch- und Kühlprodukte";
        categoryNameArray[3] = "Getränke";
        categoryNameArray[4] = "Fisch und Fleisch";
        categoryNameArray[5] = "Verpackte Produkte";
        categoryNameArray[6] = "Obst und Gemüse";

        categoryNameId = new String[category_count];
        categoryNameId[0] = "ldjiSgbebkQlHJ";
        categoryNameId[1] = "nxhRDElbjheFkv";
        categoryNameId[2] = "pHLKshvZkdKvDf";
        categoryNameId[3] = "zgDkLhwGPtSdlk";
        categoryNameId[4] = "wLd7XDpdV9Iflo";
        categoryNameId[5] = "pPtkCf8ytIG3lr";
        categoryNameId[6] = "mt6dd3tM6gf2s0";

        WriteBatch batch = firebaseFirestore.batch();
        collectionReference = firebaseFirestore.collection(FIRESTORE_EINKAUFSZETTEL_COLLECTION);
        collectionSaveReference = firebaseFirestore.collection(FIRESTORE_SAVE_EINKAUFSZETTEL_COLLECTION);
        int i = 0;
        for (String currentTitle : categoryNameArray) {
            batch.set(collectionReference.document(categoryNameId[i]), new Note(currentTitle, categoryPosition(i), Note.NOTE_NO_COLOR, Note.CATEGORY, categoryNameId[i]));
            i++;
        }
        batch.commit();

        //===================================================================================================================================================
        //Einkaufsfortschritt im Menü darstellen
        //===================================================================================================================================================
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in. Der EventListener ist leider abgestürzt ohne vorhandenen Login
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                // Kategorie grün falls alle Kategorie Items grün sind
                for (DocumentChange change : snapshots.getDocumentChanges()) {
                    Note changedNote = change.getDocument().toObject(Note.class);
                    String category = String.valueOf(changedNote.gibAdapterPos().charAt(0));
                    int countInCategory = -1;
                    int countGreenItems = 0;
                    Note categoryNote = null;
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Note note = doc.toObject(Note.class);
                        if (String.valueOf(note.gibAdapterPos().charAt(0)).equals(category)) {
                            if (note.gibType() == Note.CATEGORY)
                                categoryNote = note;

                            if (note.gibNoteColor() == Note.NOTE_COLOR_GREEN && note.gibType() == Note.NOTE)
                                countGreenItems++;
                            countInCategory++;
                        }
                    }
                    if (countInCategory == countGreenItems && countInCategory > 0) {
                        if (categoryNote.gibNoteColor() != Note.NOTE_COLOR_GREEN) {
                            collectionReference.document(categoryNote.gibId()).update(Note.NOTE_COLOR, crypt.encryptLong(Note.NOTE_COLOR_GREEN));
                        }
                    } else {
                        if (categoryNote.gibNoteColor() != Note.NOTE_NO_COLOR) {
                            collectionReference.document(categoryNote.gibId()).update(Note.NOTE_COLOR, crypt.encryptLong(Note.NOTE_NO_COLOR));
                        }
                    }
                }

                //Einkaufsfortschritt im Menü darstellen
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                int green_counter = 0;
                int size = snapshots.size() - category_count;

                for (QueryDocumentSnapshot doc : snapshots) {
                    if (crypt.decryptLong(doc.getString(Note.NOTE_COLOR)) == Note.NOTE_COLOR_GREEN && crypt.decryptLong(doc.getString(Note.TYPE)) != Note.CATEGORY) {
                        green_counter++;
                    }
                }

                MenuItem item_counter = menu.findItem(R.id.item_counter);
                MenuItem progress = menu.findItem(R.id.progress);

                DecimalFormat format = new DecimalFormat("##0.##");
                item_counter.setTitle(green_counter + "/" + size);
                if (size == 0)
                    progress.setTitle("0%");
                else
                    progress.setTitle(format.format(((double) green_counter / (double) size) * 100) + "%");
            }
        });}

        //===================================================================================================================================================
        //Adapter Funktionalitäten
        //===================================================================================================================================================
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(collectionReference.orderBy(ADAPTER_POS, Query.Direction.DESCENDING), Note.class)
                .build();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new NoteAdapter(options, this);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof NoteAdapter.CategoryHolder) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                final Note note = adapter.getSnapshots().get(viewHolder.getAdapterPosition());

                soundPool.play(deleteSound, 0.4F, 0.4F, 0, 0, 1);
                adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).getReference().delete();
                Snackbar.make(recyclerView, "Einkaufseintrag gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                collectionReference.document(note.gibId()).set(note);
                            }
                        }).show();
                }
            }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note, String id) {
                final Note noteClicked = note;
                if (note.gibType() == Note.NOTE) {
                    if (note.gibNoteColor() == note.NOTE_NO_COLOR) {
                        soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                        collectionReference.document(id).update(Note.NOTE_COLOR, crypt.encryptLong(note.NOTE_COLOR_GREEN));
                    } else {
                        soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                        collectionReference.document(id).update(Note.NOTE_COLOR, crypt.encryptLong(note.NOTE_NO_COLOR));
                    }
                }
            }
        });

        adapter.setOnLongItemClickListener(new NoteAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Note note, String id) {
                final Note noteClicked = note;
                if (note.gibType() == Note.NOTE) {
                    if (note.gibNoteColor() != note.NOTE_COLOR_YELLOW) {
                        soundPool.play(turn_orangeSound, 0.2F, 0.2F, 0, 0, 1);
                        collectionReference.document(id).update(Note.NOTE_COLOR, crypt.encryptLong(note.NOTE_COLOR_YELLOW));
                    }
                } else {
                    which_category = String.valueOf(note.gibAdapterPos().charAt(0));
                    customSpeechRecognition.startSpeechRequest(which_category , note);
                }
            }
        });

        fab_done = findViewById(R.id.fab_shoppingDone);
        fab_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                final String currentNutzerToCheck = sharedPreferences.getString(SHARED_PREF_NAME, SHARED_PREF_NO_NUTZER);

                if (currentNutzerToCheck.equals(SHARED_PREF_NO_NUTZER)) {
                    Snackbar.make(fab_done, "Nutzer auswählen oder erstellen", Snackbar.LENGTH_LONG);
                } else {
                    final String currentNutzer = crypt.decryptString(currentNutzerToCheck);
                    final List<Note> notesGreen = new ArrayList<>();
                    final WriteBatch batch = firebaseFirestore.batch();
                    soundPool.play(finishSound, 0.2F, 0.2F, 0, 0, 1);

                    NewEinkaufFinishedDialog rechnungDialog = NewEinkaufFinishedDialog.newInstance(currentNutzer, new NewEinkaufFinishedDialog.OnDialogFinishedListener() {
                        @Override
                        public void onDialogFinished(final DocumentReference docRefOfAddedRechnung) {
                            for (Note currentNote : adapter.getSnapshots()) {
                                if (currentNote.gibNoteColor() == Note.NOTE_COLOR_GREEN && currentNote.gibType() == Note.NOTE) {
                                    batch.delete(collectionReference.document(currentNote.gibId()));
                                    notesGreen.add(currentNote);
                                }
                                if (currentNote.gibNoteColor() == Note.NOTE_COLOR_GREEN && currentNote.gibType() == Note.CATEGORY)
                                    batch.update(collectionReference.document(currentNote.gibId()), Note.NOTE_COLOR, crypt.encryptLong(Note.NOTE_NO_COLOR));
                            }
                            batch.commit();
                            Snackbar.make(fab_done, "Alle Erledigten gelöscht!", Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                            WriteBatch batch = firebaseFirestore.batch();
                                            for (Note note : notesGreen)
                                                batch.set(collectionReference.document(note.gibId()), note);
                                            CollectionReference collectionEinkaufszettelBillReference = firebaseFirestore.collection(FIRESTORE_EINKAUFSZETTEL_BILL_COLLECTION);
                                            batch.delete(collectionEinkaufszettelBillReference.document(docRefOfAddedRechnung.getId()));
                                            batch.commit();
                                        }
                                    }).show();
                        }
                    });
                    rechnungDialog.show(getSupportFragmentManager(), "EinkaufFinishedDialog");
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this, "Neu starten bitte!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.speichern) {
            Snackbar.make(fab_done, "Speichern", Snackbar.LENGTH_LONG).setAction("BESTÄTIGEN", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                    collectionSaveReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                WriteBatch batch = firebaseFirestore.batch();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    batch.delete(collectionSaveReference.document(document.getId()));
                                }
                                for (Note note : adapter.getSnapshots()) {
                                    if (note.gibType() == Note.NOTE)
                                        batch.set(collectionSaveReference.document(note.gibId()), note);
                                }
                                batch.commit();
                            }
                        }
                    });
                }
            }).show();
            return true;
        }
        if (id == R.id.laden) {
            Snackbar.make(fab_done, "Laden", Snackbar.LENGTH_LONG).setAction("BESTÄTIGEN", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                    collectionSaveReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            collectionSaveReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        WriteBatch batch = firebaseFirestore.batch();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            DocumentReference docRef = collectionReference.document();
                                            Note noteToAdd = document.toObject(Note.class);
                                            noteToAdd.setzeId(docRef.getId());
                                            batch.set(collectionReference.document(docRef.getId()), noteToAdd);
                                        }
                                        batch.commit();
                                    }
                                }
                            });
                        }
                    });
                }
            }).show();

        }
        if (id == R.id.geldmanagment) {

            //Die Eingabe der Passwortes wurde ausgeklammert weil zu umständlich zu bedienen

            /*PassphrasenDialog passphrasenDialog = PassphrasenDialog.newInstance(MainActivity.this, new PassphrasenDialog.OnDialogFinishedListener() {
                       @Override
                       public void onDialogFinished(String passphrase) {
                           Intent intent = new Intent(MainActivity.this, Geldmanagment.class);
                           intent.putExtra(PASSPHRASE, passphrase);
                           startActivity(intent);
                       }
                   });
                   passphrasenDialog.show(getSupportFragmentManager(), "PassphrasenDialog");*/

            Intent intent = new Intent(MainActivity.this, Geldmanagment.class);
            intent.putExtra(PASSPHRASE, "gus321butzel0");
            startActivity(intent);
            return true;
        }
        if (id == R.id.signout) {
            securityHandling.firebaseSignOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    private String categoryPosition(int pos) {

        switch (pos) {
            case 0:
                return "AA";
            case 1:
                return "BB";
            case 2:
                return "CC";
            case 3:
                return "DD";
            case 4:
                return "EE";
            case 5:
                return "FF";
            case 6:
                return "GG";
            case 7:
                return "HH";
            case 8:
                return "II";
            case 9:
                return "JJ";
            case 10:
                return "KK";
            case 11:
                return "LL";
            case 12:
                return "MM";
            case 13:
                return "NN";
            default:
                return null;
        }

    }
}
