package com.example.android.interaktivereinkaufszettel;

import com.google.firebase.firestore.Exclude;

import java.util.List;

public class Note {

    static final int NOTE_REMOVE = 0;
    static final int NOTE_ADD = 1;

    static final long NOTE_NO_COLOR = 0;
    static final long NOTE_COLOR_GREEN = 1;
    static final long NOTE_COLOR_YELLOW = 2;

    private long adapterPos=0;
    private String id = "";
    private String content = "default";
    private long noteColor = NOTE_NO_COLOR;

    public Note() {
    }

    public Note(String content, String id, long noteColor, long adapterPos) {
        this.content = content;
        this.id = id;
        this.noteColor = noteColor;
        this.adapterPos = adapterPos;
    }

    public Note(String content, long adapterPos) {
        this.content = content;
        this.adapterPos = adapterPos;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public long getNoteColor() {
        return noteColor;
    }

    public long getAdapterPos() {
        return adapterPos;
    }

    public void setAdapterPos(long adapterPos) {
        this.adapterPos = adapterPos;
    }
}
