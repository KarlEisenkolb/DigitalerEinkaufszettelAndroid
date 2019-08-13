package com.example.android.interaktivereinkaufszettel;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    final static String FIRESTORE_EINKAUFSZETTEL_COLLECTION = "Einkaufszettel";
    final static String FIRESTORE_SAVE_EINKAUFSZETTEL_COLLECTION = "SaveEinkaufszettel";
    final static int RC_SIGN_IN = 0;

    static String which_category = "default";
    private CustomFirebaseSecurityHandling securityHandling;
    private AudioManager audio;
    private SoundPool soundPool;
    private int deleteSound, finishSound, turn_orangeSound, turn_greenSound, undoSound;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String[] categoryNameArray;
    private CollectionReference collectionReference;
    private CollectionReference collectionSaveReference;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private Menu menu;
    private FloatingActionButton fab_done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Crypt crypt = new Crypt();

        securityHandling = new CustomFirebaseSecurityHandling(this);

        final CustomSpeechRecognition customSpeechRecognition = new CustomSpeechRecognition(this);

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

        //Hardcoded Kategorien festlegen
        final int category_count = 7;
        categoryNameArray = new String[category_count];

        categoryNameArray[0] = "Sonstiges";
        categoryNameArray[1] = "Drogerieprodukte";
        categoryNameArray[2] = "Milch- und Kühlprodukte";
        categoryNameArray[3] = "Getränke";
        categoryNameArray[4] = "Fisch und Fleisch";
        categoryNameArray[5] = "Verpackte Produkte";
        categoryNameArray[6] = "Obst und Gemüse";

        WriteBatch batch = firebaseFirestore.batch();
        collectionReference = firebaseFirestore.collection(FIRESTORE_EINKAUFSZETTEL_COLLECTION);
        collectionSaveReference = firebaseFirestore.collection(FIRESTORE_SAVE_EINKAUFSZETTEL_COLLECTION);
        int i = 0;
        for (String currentTitle : categoryNameArray) {
            batch.set(collectionReference.document(categoryNameArray[i]), new Note(currentTitle, categoryPosition(i), Note.NOTE_NO_COLOR, Note.CATEGORY));
            i++;
        }
        batch.commit();

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                int green_counter = 0;
                int size = queryDocumentSnapshots.size() - category_count;

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.getLong("noteColor") == Note.NOTE_COLOR_GREEN) {
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
        });

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(collectionReference.orderBy("adapterPos", Query.Direction.DESCENDING), Note.class)
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
                                collectionReference.add(note);
                            }
                        }).show();
                }
            }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note, String id) {
                if (note.getType() == Note.NOTE) {
                    if (note.getNoteColor() == note.NOTE_NO_COLOR) {
                        soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                        collectionReference.document(id).update("noteColor", note.NOTE_COLOR_GREEN);
                    } else {
                        soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                        collectionReference.document(id).update("noteColor", note.NOTE_NO_COLOR);
                    }
                }
            }
        });

        adapter.setOnLongItemClickListener(new NoteAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Note note, String id) {
                if (note.getType() == Note.NOTE) {
                    if (note.getNoteColor() != note.NOTE_COLOR_YELLOW) {
                        soundPool.play(turn_orangeSound, 0.2F, 0.2F, 0, 0, 1);
                        collectionReference.document(id).update("noteColor", note.NOTE_COLOR_YELLOW);
                    }
                } else {
                    which_category = String.valueOf(note.getAdapterPos().charAt(0));
                    customSpeechRecognition.startSpeechRequest(which_category);
                }
            }
        });

        fab_done = findViewById(R.id.fab_shoppingDone);
        fab_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<Note> notesGreen = new ArrayList<>();
                WriteBatch batch = firebaseFirestore.batch();

                soundPool.play(finishSound, 0.2F, 0.2F, 0, 0, 1);

                for (int i = 0; i < adapter.getSnapshots().size(); i++) {
                    if (adapter.getSnapshots().get(i).getNoteColor() == Note.NOTE_COLOR_GREEN) {
                        batch.delete(collectionReference.document(adapter.getSnapshots().getSnapshot(i).getId()));
                        notesGreen.add(adapter.getSnapshots().get(i));
                    }
                }
                batch.commit();


                Snackbar.make(fab_done, "Alle Erledigten gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                WriteBatch batch = firebaseFirestore.batch();
                                for (Note note : notesGreen)
                                    batch.set(collectionReference.document(), note);
                                batch.commit();
                            }
                        }).show();
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
                // ...
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
                                    if (note.getType() == Note.NOTE)
                                        batch.set(collectionSaveReference.document(), note);
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
                                            batch.set(collectionReference.document(), document.toObject(Note.class));
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
        if (id == R.id.messageWhatsApp) {
            openWhatsappContact("4915738975901");
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    void openWhatsappContact(String number) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Einkaufszettel");
        sendIntent.setType("text/plain");
        sendIntent.putExtra("jid", number + "@s.whatsapp.net");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
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

    private int numberOfItemsInCategory(String category) {
        int count = -1;
        for (Note note : adapter.getSnapshots()) {
            if (String.valueOf(note.getAdapterPos().charAt(0)).equals(category))
                count++;
        }
        return count;
    }


}
