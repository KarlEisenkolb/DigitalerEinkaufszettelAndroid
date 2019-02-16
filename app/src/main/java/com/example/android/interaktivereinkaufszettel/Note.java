package com.example.android.interaktivereinkaufszettel;

public class Note {

    static final long NOTE_NO_COLOR=0;
    static final long NOTE_COLOR_GREEN=1;
    static final  long NOTE_COLOR_YELLOW=2;

    private String id = "";
    private String content = "default";
    private long noteColor = NOTE_NO_COLOR;

    public Note(){}

    public Note(String content, String id, long noteColor) {
        this.id = id;
        this.content = content;
        this.noteColor = noteColor;
    }

    public Note(String content){
        this.content = content;
    }

    public String getId(){return id;}
    public String getContent() {return content;}
    public long getNoteColor() {return noteColor;}
}
