package com.example.android.interaktivereinkaufszettel;

public class Note {

    static final long NOTE_NO_COLOR = 0;
    static final long NOTE_COLOR_GREEN = 1;
    static final long NOTE_COLOR_YELLOW = 2;

    private long adapterPos=0;
    private String content = "default";
    private long noteColor = NOTE_NO_COLOR;

    public Note() {
    }

    public Note(String content, long adapterPos,  long noteColor) {
        this.content = content;
        this.noteColor = noteColor;
        this.adapterPos = adapterPos;
    }

    public Note(String content, long adapterPos) {
        this.content = content;
        this.adapterPos = adapterPos;
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

}
