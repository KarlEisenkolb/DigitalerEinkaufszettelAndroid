package com.example.android.interaktivereinkaufszettel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private AudioManager audio;
    private SoundPool soundPool;
    private int deleteSound, finishSound, turn_orangeSound, turn_greenSound, undoSound;

    public static final String DOC_REFERENCE = "EinkaufszettelCollectionTest";
    static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    static CollectionReference firebaseCollectionReference = firebaseFirestore.collection(DOC_REFERENCE);
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(firebaseCollectionReference.orderBy("adapterPos", Query.Direction.DESCENDING), Note.class)
                .build();

        List<String> category = new ArrayList<>();
        category.add("Obst und Gemüse");
        category.add("Fisch und Fleisch");
        category.add("Verpackte Produkte");
        category.add("Getränke");
        category.add("Milch- und Kühlprodukte");
        category.add("Drogerieprodukte");

        LinearLayout main_layout = findViewById(R.id.main_layout);
        for (String currentCategory : category) {


            TextView categoryView = new TextView(new ContextThemeWrapper(this, R.style.categoryStyle), null, 0);
            categoryView.setText(currentCategory);
            categoryView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) dpToPx(this, 90)));
            int dp = (int) dpToPx(this, 15);
            categoryView.setPadding(dp, dp, dp, dp);
            categoryView.setBackgroundColor(Color.parseColor("#212121"));
            main_layout.addView(categoryView);

        }

        final RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new NoteAdapter(options);
        recyclerView.setAdapter(adapter);
        main_layout.addView(recyclerView);

        View pufferView = new View(this);
        pufferView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) dpToPx(this, 200)));
        main_layout.addView(pufferView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

             @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final Note note = adapter.getSnapshots().get(viewHolder.getAdapterPosition());

                soundPool.play(deleteSound, 0.4F, 0.4F, 0, 0, 1);
                 adapter.deleteNote(viewHolder.getAdapterPosition());
                Snackbar.make(recyclerView, "Einkaufseintrag gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                firebaseCollectionReference.add(note);
                            }
                        }).show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note, String id) {
                if (note.getNoteColor() == note.NOTE_NO_COLOR) {
                    soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                    firebaseCollectionReference.document(id).update("noteColor", note.NOTE_COLOR_GREEN);
                } else {
                    soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                    firebaseCollectionReference.document(id).update("noteColor", note.NOTE_NO_COLOR);
                }
            }
        });

        adapter.setOnLongItemClickListener(new NoteAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Note note, String id) {
                if (note.getNoteColor() != note.NOTE_COLOR_YELLOW) {
                    soundPool.play(turn_orangeSound, 0.2F, 0.2F, 0, 0, 1);
                    firebaseCollectionReference.document(id).update("noteColor", note.NOTE_COLOR_YELLOW);
                }
            }
        });

        checkPermission();

        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);


        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null)
                    firebaseCollectionReference.add(new Note(matches.get(0), adapter.getSnapshots().size()));
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });


        FloatingActionButton fab_mic = findViewById(R.id.fab_mic);
        fab_mic.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                                       }
                                   }
        );

        FloatingActionButton fab_done = findViewById(R.id.fab_shoppingDone);
        fab_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<Note> notesGreen = new ArrayList();
                WriteBatch batch = firebaseFirestore.batch();

                soundPool.play(finishSound, 0.2F, 0.2F, 0, 0, 1);
                for(int i=0; i<adapter.getSnapshots().size(); i++){
                    if (adapter.getSnapshots().get(i).getNoteColor() == Note.NOTE_COLOR_GREEN) {
                        batch.delete(firebaseCollectionReference.document(adapter.getSnapshots().getSnapshot(i).getId()));
                        notesGreen.add(adapter.getSnapshots().get(i));
                    }
                }

                batch.commit();

                Snackbar.make(recyclerView, "Alle Erledigten gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                for (Note currentNote : notesGreen){
                                    firebaseCollectionReference.add(new Note(currentNote.getContent(), currentNote.getAdapterPos(), Note.NOTE_COLOR_GREEN));
                                }
                            }
                        }).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.messageWhatsApp) {
            openWhatsappContact("4915738975901");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        for(int i=1; i<=adapter.getSnapshots().size(); i++){
            firebaseCollectionReference.document(adapter.getSnapshots().getSnapshot(i-1).getId()).update("adapterPos", adapter.getSnapshots().size()-i);
        }
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
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    public static float dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }

}
