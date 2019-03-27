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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
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

import javax.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    static int which_category=0;
    private AudioManager audio;
    private SoundPool soundPool;
    private int deleteSound, finishSound, turn_orangeSound, turn_greenSound, undoSound;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String[] categoryNameArray;
    private NoteAdapter[] adapterArray;
    private CollectionReference[] referenceArray;
    private Button[] categoryButtonArray;
    private RecyclerView[] recyclerViewArray;

    public static float dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                    referenceArray[which_category].add(new Note(matches.get(0), adapterArray[which_category].getSnapshots().size()));
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });

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

        int category_count =7;
        categoryNameArray = new String[category_count];
        categoryNameArray[0]="Obst und Gemüse";
        categoryNameArray[1]="Fisch und Fleisch";
        categoryNameArray[2]="Verpackte Produkte";
        categoryNameArray[3]="Getränke";
        categoryNameArray[4]="Milch- und Kühlprodukte";
        categoryNameArray[5]="Drogerieprodukte";
        categoryNameArray[6]="Sonstiges";

        adapterArray = new NoteAdapter[category_count];
        referenceArray = new CollectionReference[category_count];
        categoryButtonArray = new Button[category_count];
        recyclerViewArray = new RecyclerView[category_count];

        LinearLayout main_layout = findViewById(R.id.main_layout);
        for (int i=0; i<categoryNameArray.length; i++) {
            final int final_i = i;

            referenceArray[i] = firebaseFirestore.collection(categoryNameArray[i]);

            referenceArray[i].addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    adjustRecyclerHeight();
                }
            });

            FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                    .setQuery(referenceArray[i].orderBy("adapterPos", Query.Direction.DESCENDING), Note.class)
                    .build();

            categoryButtonArray[i] = new Button(new ContextThemeWrapper(this, R.style.categoryStyle), null, 0);
            categoryButtonArray[i].setText(categoryNameArray[i]);
            categoryButtonArray[i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) dpToPx(this, 90)));
            int dp = (int) dpToPx(this, 15);
            categoryButtonArray[i].setPadding(dp, dp, dp, dp);
            categoryButtonArray[i].setBackgroundColor(Color.parseColor("#757575"));
            categoryButtonArray[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    which_category = final_i;
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    return true;
                }
            });
            main_layout.addView(categoryButtonArray[i]);

            recyclerViewArray[i] = new RecyclerView(this);
            recyclerViewArray[i].setLayoutManager(new LinearLayoutManager(this));
            recyclerViewArray[i].setNestedScrollingEnabled(false);
            recyclerViewArray[i].setMinimumHeight((int) dpToPx(this, 1));
            adapterArray[i] = new NoteAdapter(options);
            recyclerViewArray[i].setAdapter(adapterArray[i]);

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                    final Note note = adapterArray[final_i].getSnapshots().get(viewHolder.getAdapterPosition());

                    soundPool.play(deleteSound, 0.4F, 0.4F, 0, 0, 1);
                    adapterArray[final_i].getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            reorderFirestore(final_i);
                        }
                    });
                    Snackbar.make(recyclerViewArray[final_i], "Einkaufseintrag gelöscht!", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                    referenceArray[final_i].add(note);
                                }
                            }).show();
                }
            }).attachToRecyclerView(recyclerViewArray[i]);

            adapterArray[i].setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Note note, String id) {
                    if (note.getNoteColor() == note.NOTE_NO_COLOR) {
                        soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                        referenceArray[final_i].document(id).update("noteColor", note.NOTE_COLOR_GREEN);
                    } else {
                        soundPool.play(turn_greenSound, 0.07F, 0.07F, 0, 0, 1);
                        referenceArray[final_i].document(id).update("noteColor", note.NOTE_NO_COLOR);
                    }
                }
            });

            adapterArray[i].setOnLongItemClickListener(new NoteAdapter.OnLongItemClickListener() {
                @Override
                public void onLongItemClick(Note note, String id) {
                    if (note.getNoteColor() != note.NOTE_COLOR_YELLOW) {
                        soundPool.play(turn_orangeSound, 0.2F, 0.2F, 0, 0, 1);
                        referenceArray[final_i].document(id).update("noteColor", note.NOTE_COLOR_YELLOW);
                    }
                }
            });
            main_layout.addView(recyclerViewArray[i]);
        }

        View pufferView = new View(this);
        pufferView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) dpToPx(this, 200)));
        main_layout.addView(pufferView);

        checkPermission();

        final FloatingActionButton fab_done = findViewById(R.id.fab_shoppingDone);
        fab_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<List<Note>> notesGreenOuter = new ArrayList<>();
                WriteBatch batch = firebaseFirestore.batch();

                soundPool.play(finishSound, 0.2F, 0.2F, 0, 0, 1);
                for (int n=0; n<categoryNameArray.length; n++){
                    List<Note> notesGreenInner = new ArrayList<>();
                for(int i=0; i<adapterArray[n].getSnapshots().size(); i++){
                    if (adapterArray[n].getSnapshots().get(i).getNoteColor() == Note.NOTE_COLOR_GREEN) {
                        batch.delete(referenceArray[n].document(adapterArray[n].getSnapshots().getSnapshot(i).getId()));
                        notesGreenInner.add(adapterArray[n].getSnapshots().get(i));
                    }
                    notesGreenOuter.add(notesGreenInner);
                }}

                batch.commit();

                Snackbar.make(fab_done, "Alle Erledigten gelöscht!", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                soundPool.play(undoSound, 0.1F, 0.1F, 0, 0, 1);
                                WriteBatch batch = firebaseFirestore.batch();
                                int n=0;
                                int i=0;
                                for (List<Note> currentGreenInner : notesGreenOuter){
                                    for(Note currentNote : currentGreenInner){
                                        batch.set(referenceArray[n].document(adapterArray[n].getSnapshots().getSnapshot(i).getId()) ,new Note(currentNote.getContent(), currentNote.getAdapterPos(), Note.NOTE_COLOR_GREEN));
                                }}
                                batch.commit();
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
        for (int i=0; i<categoryNameArray.length; i++)
        adapterArray[i].startListening();
    }

    private void reorderFirestore(int n){
        WriteBatch batch = firebaseFirestore.batch();
        //for (int n=0; n<categoryNameArray.length; n++){
            for(int i=1; i<=adapterArray[n].getSnapshots().size(); i++){
                batch.update(referenceArray[n].document(adapterArray[n].getSnapshots().getSnapshot(i-1).getId()),"adapterPos", adapterArray[n].getSnapshots().size()-i);

            }//}
        batch.commit();
    }

    private void adjustRecyclerHeight(){
        for (int i=0; i<categoryNameArray.length; i++){
            recyclerViewArray[i].setMinimumHeight((int) dpToPx(this, 55*adapterArray[i].getSnapshots().size()+2));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (int i=0; i<categoryNameArray.length; i++)
        adapterArray[i].stopListening();
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

}
