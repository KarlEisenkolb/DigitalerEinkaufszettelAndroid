package com.example.android.interaktivereinkaufszettel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.MainActivity.FIRESTORE_EINKAUFSZETTEL_COLLECTION;

public class CustomSpeechRecognition {

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private String which_category = "default";
    private Note categoryNote;

    public CustomSpeechRecognition(Context ctx) {

        collectionReference = firebaseFirestore.collection(FIRESTORE_EINKAUFSZETTEL_COLLECTION);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(ctx);

        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
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

                long pos = System.currentTimeMillis();

                Log.d("onResults", "test: " + pos);
                //displaying the first match
                if (matches != null) {
                    DocumentReference docRef = collectionReference.document();
                    docRef.set(new Note(matches.get(0), which_category + pos, docRef.getId()));

                    if (categoryNote.gibNoteColor() != Note.NOTE_NO_COLOR) {
                        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
                        collectionReference.document(categoryNote.gibId()).update(Note.NOTE_COLOR, crypt.encryptLong(Note.NOTE_NO_COLOR));
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });

    }

    public void startSpeechRequest(String which_category, Note categoryNote){
    this.which_category = which_category;
    this.categoryNote = categoryNote;
    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

}
