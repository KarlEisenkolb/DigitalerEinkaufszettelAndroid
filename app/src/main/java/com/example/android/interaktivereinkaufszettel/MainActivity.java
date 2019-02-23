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
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private AudioManager audio;
    private SoundPool soundPool;
    private int deleteSound, finishSound, turn_orangeSound, turn_greenSound, undoSound;

    private List<Note> notes;
    public static final String DOC_REFERENCE = "EinkaufszettelCollection";
    static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    static CollectionReference firebaseCollectionReference = firebaseFirestore.collection(DOC_REFERENCE);
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notes = new ArrayList<>();

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

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new NoteAdapter(notes);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final Note note = adapter.getNoteAt(viewHolder.getAdapterPosition());
                final int adapterPos = (int) note.getAdapterPos();

                soundPool.play(deleteSound, 0.4F, 0.4F, 0, 0, 1);
                firebaseCollectionReference.document(adapter.getIdAt(viewHolder.getAdapterPosition())).delete();
                adjustListPositionsFirestore(adapterPos, Note.NOTE_REMOVE);
                Snackbar.make(recyclerView, "Einkaufseintrag gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                firebaseCollectionReference.add(note);
                                adjustListPositionsFirestore(adapterPos, Note.NOTE_ADD);
                            }
                        }).show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                if (note.getNoteColor() == note.NOTE_NO_COLOR) {
                    soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                    firebaseCollectionReference.document(note.getId()).update("noteColor", note.NOTE_COLOR_GREEN);
                } else {
                    soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                    firebaseCollectionReference.document(note.getId()).update("noteColor", note.NOTE_NO_COLOR);
                }
            }
        });

        adapter.setOnLongItemClickListener(new NoteAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Note note) {
                if (note.getNoteColor() != note.NOTE_COLOR_YELLOW) {
                    soundPool.play(turn_orangeSound, 0.2F, 0.2F, 0, 0, 1);
                    firebaseCollectionReference.document(note.getId()).update("noteColor", note.NOTE_COLOR_YELLOW);
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
                    firebaseCollectionReference.add(new Note(matches.get(0), 0));
                adjustListPositionsFirestore(0, Note.NOTE_ADD);
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
                final List<Note> notesBackup = new ArrayList<>();
                soundPool.play(finishSound, 0.2F, 0.2F, 0, 0, 1);
                for (Note currentNote : notes) {
                    if (currentNote.getNoteColor() == Note.NOTE_COLOR_GREEN) {
                        firebaseCollectionReference.document(currentNote.getId()).delete();
                        notesBackup.add(currentNote);
                    }
                }

                Snackbar.make(recyclerView, "Alle Erledigten gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                for (Note currentBackupNote : notesBackup) {
                                    Log.d("Inhalt notesBackup AddingFirebaseSchleife", currentBackupNote.getContent() + " " + currentBackupNote.getNoteColor());
                                    firebaseCollectionReference.add(currentBackupNote);
                                }
                            }
                        }).show();
            }
        });
    }

    private void adjustListPositionsFirestore(int position, int removeOrAdd) {
        int positionAdjuster = 0;

        if (removeOrAdd == Note.NOTE_REMOVE)
            positionAdjuster = 1;

            for (int i = (position + positionAdjuster); i <= (notes.size() - 1); i++) {
                switch(removeOrAdd) {
                    case Note.NOTE_ADD:
                    firebaseCollectionReference.document(notes.get(i).getId()).update("adapterPos", notes.get(i).getAdapterPos() + 1);
                    break;

                    case Note.NOTE_REMOVE:
                    firebaseCollectionReference.document(notes.get(i).getId()).update("adapterPos", notes.get(i).getAdapterPos() - 1);
                    break;
                }
        }
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
        notes.clear();
        firebaseCollectionReference.orderBy("adapterPos").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                //Toast.makeText(MainActivity.this, "onEventTriggered", Toast.LENGTH_SHORT).show();
                if (e != null) {
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                    Note note = extractData(dc);
                    int adapterPos = dc.getDocument().getLong("adapterPos").intValue();

                    switch (dc.getType()) {
                        case ADDED:
                            Log.d("Firebase Added ", "at adapterPos " + adapterPos);
                            notes.add(adapterPos, note);
                            adapter.notifyItemInserted(adapterPos);
                            break;
                        case MODIFIED:
                            Log.d("Firebase Modified ", "at adapterPos " + adapterPos);
                            notes.set(adapterPos, note);
                            adapter.notifyItemChanged(adapterPos);
                            break;
                        case REMOVED:
                            Log.d("Firebase removed ", "at adapterPos " + adapterPos);
                            notes.remove(adapterPos);
                            adapter.notifyItemRemoved(adapterPos);
                            break;
                    }
                }

                for (int i = 0; i < notes.size(); i++) {
                    Note test = notes.get(i);
                    Log.d("NoteListLog", test.getContent() + " " + test.getId() + " " + test.getNoteColor() + " " + test.getAdapterPos());
                }
            }
        });
    }


    private Note extractData(DocumentChange dc) {

        String content = dc.getDocument().getString("content");
        String id = dc.getDocument().getId();
        long noteColor = dc.getDocument().getLong("noteColor");
        int adapterPos = dc.getDocument().getLong("adapterPos").intValue();

        Log.d("Firebase extractData", content + " " + id + " " + noteColor + " " + adapterPos);
        return new Note(content, id, noteColor, adapterPos);
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
