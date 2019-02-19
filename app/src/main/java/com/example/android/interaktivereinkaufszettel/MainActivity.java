package com.example.android.interaktivereinkaufszettel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private AudioManager audio;
    private SoundPool soundPool;
    private int deleteSound, finishSound, turn_orangeSound, undoSound;

    static List<Note> notes = new ArrayList<>();
    //static List<Note> notesBackup = new ArrayList<>();
    public static final String DOC_REFERENCE = "EinkaufszettelCollection";
    static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    static CollectionReference firebaseCollectionReference = firebaseFirestore.collection(DOC_REFERENCE);
    static NoteAdapter adapter = new NoteAdapter();

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
        undoSound = soundPool.load(this, R.raw.undo, 1);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final Note note = adapter.getNoteAt(viewHolder.getAdapterPosition());
                soundPool.play(deleteSound, 1, 1, 0, 0, 1);
                firebaseCollectionReference.document(adapter.getIdAt(viewHolder.getAdapterPosition())).delete();
                Snackbar.make(recyclerView, "Einkaufseintrag gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 1, 1, 0, 0, 1);
                                firebaseCollectionReference.add(note);
                            }
                        }).show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                if (note.getNoteColor() == note.NOTE_NO_COLOR)
                    firebaseCollectionReference.document(note.getId()).update("noteColor", note.NOTE_COLOR_GREEN);
                else
                    firebaseCollectionReference.document(note.getId()).update("noteColor", note.NOTE_NO_COLOR);
            }
        });

        adapter.setOnLongItemClickListener(new NoteAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Note note) {
                if (note.getNoteColor() != note.NOTE_COLOR_YELLOW){
                    soundPool.play(turn_orangeSound, 1, 1, 0, 0, 1);
                    firebaseCollectionReference.document(note.getId()).update("noteColor", note.NOTE_COLOR_YELLOW);
            }}
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
            public void onReadyForSpeech(Bundle params){}
            @Override
            public void onBeginningOfSpeech(){}
            @Override
            public void onRmsChanged(float rmsdB){}
            @Override
            public void onBufferReceived(byte[] buffer){}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onError(int error) {}

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null)
                firebaseCollectionReference.add(new Note(matches.get(0)));
            }

            @Override
            public void onPartialResults(Bundle bundle) {}
            @Override
            public void onEvent(int i, Bundle bundle) {}
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
                final List<Note> notesBackup = new ArrayList<>();
                soundPool.play(finishSound, 1, 1, 0, 0, 1);
                for (Note currentNote : notes){
                    if (currentNote.getNoteColor() == Note.NOTE_COLOR_GREEN){
                        firebaseCollectionReference.document(currentNote.getId()).delete();
                        notesBackup.add(currentNote);}
                }

                Snackbar.make(recyclerView, "Alle Erledigten gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 1, 1, 0, 0, 1);
                                for (Note currentBackupNote : notesBackup){
                                    Log.d("Inhalt notesBackup AddingFirebaseSchleife",currentBackupNote.getContent()+" "+currentBackupNote.getNoteColor());
                                    firebaseCollectionReference.add(currentBackupNote);}
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
        if (id == R.id.messageWhatsApp){
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
        firebaseCollectionReference.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                //Toast.makeText(MainActivity.this, "onEventTriggered", Toast.LENGTH_SHORT).show();
                if (e != null) {
                    return;
                }
                notes.clear();
                Log.d("Tick notes.clear","notes.clear");
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String content = documentSnapshot.getString("content");
                    String id = documentSnapshot.getId();
                    long noteColor = documentSnapshot.getLong("noteColor");

                    Log.d("Tick queryExtraktion",content+ " " + id+ " " + noteColor);

                    Note note = new Note(content, id, noteColor);
                    notes.add(note);
                }
                adapter.setNotes(notes);
            }
        });
    }

    void openWhatsappContact(String number) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, " ");
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
}
