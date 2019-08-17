package com.example.android.interaktivereinkaufszettel;

public class Note {

    static final long NOTE_NO_COLOR = 0;
    static final long NOTE_COLOR_GREEN = 1;
    static final long NOTE_COLOR_YELLOW = 2;
    static final long NOTE = 0;
    static final long CATEGORY = 1;

    static final String ADAPTER_POS = "inXEAIWqkta";
    static final String CONTENT = "bZI0mGySHpL";
    static final String NOTE_COLOR = "eiz1WXHTZ3o";
    static final String TYPE ="lMIs9w2TzUP";

    private String inXEAIWqkta = "0"; //adapterPos
    private String bZI0mGySHpL = "default"; //content
    private long eiz1WXHTZ3o = NOTE_NO_COLOR; //noteColor
    private long lMIs9w2TzUP = NOTE; //type

    public Note() {
    }

    public Note(String content, String adapterPos, long noteColor, long type) {
        this.bZI0mGySHpL = content;
        this.eiz1WXHTZ3o = noteColor;
        this.inXEAIWqkta = adapterPos;
        this.lMIs9w2TzUP = type;
    }

    public Note(String content, String adapterPos) {
        this.bZI0mGySHpL = content;
        this.inXEAIWqkta = adapterPos;
    }

    public String getinXEAIWqkta() {
        return inXEAIWqkta;
    }

    public String getbZI0mGySHpL() {
        return bZI0mGySHpL;
    }

    public long geteiz1WXHTZ3o() {
        return eiz1WXHTZ3o;
    }

    public long getlMIs9w2TzUP() {
        return lMIs9w2TzUP;
    }

    public String gibContent() {
        return getbZI0mGySHpL();
    }

    public String gibAdapterPos() {
        return getinXEAIWqkta();
    }

    public long gibType() {
        return getlMIs9w2TzUP();
    }

    public long gibNoteColor() {
        return geteiz1WXHTZ3o();
    }


}
