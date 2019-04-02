package com.example.android.interaktivereinkaufszettel;

public class Note {

    static final long NOTE_NO_COLOR = 0;
    static final long NOTE_COLOR_GREEN = 1;
    static final long NOTE_COLOR_YELLOW = 2;
    static final long NOTE = 0;
    static final long CATEGORY = 1;

    private String adapterPos = "0";
    private String content = "default";
    private long noteColor = NOTE_NO_COLOR;
    private long type = NOTE;

    public Note() {
    }

    public Note(String content, String adapterPos, long noteColor, long type) {
        this.content = content;
        this.noteColor = noteColor;
        this.adapterPos = adapterPos;
        this.type = type;
    }

    public Note(String content, String adapterPos) {
        this.content = content;
        this.adapterPos = adapterPos;
    }

    public String getContent() {
        return content;
    }

    public String getAdapterPos() {
        return adapterPos;
    }

    public void setAdapterPos(String adapterPos) {
        this.adapterPos = adapterPos;
    }

    public long getType() {
        return type;
    }

    public long getNoteColor() {
        return noteColor;
    }


}
