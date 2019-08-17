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

    private String bZI0mGySHpL; //content
    private String inXEAIWqkta; //adapterPos
    private String eiz1WXHTZ3o; //noteColor
    private String lMIs9w2TzUP; //type

    public Note() {
    }

    public Note(String content, String adapterPos, long noteColor, long type) {
        Crypt crypt = new Crypt();
        this.bZI0mGySHpL = crypt.encryptString(content);
        this.inXEAIWqkta = adapterPos;
        this.eiz1WXHTZ3o = crypt.encryptLong(noteColor);
        this.lMIs9w2TzUP = crypt.encryptLong(type);
    }

    public Note(String content, String adapterPos) {
        Crypt crypt = new Crypt();
        this.bZI0mGySHpL = crypt.encryptString(content);
        this.inXEAIWqkta = adapterPos;
        this.eiz1WXHTZ3o = crypt.encryptLong(NOTE_NO_COLOR);
        this.lMIs9w2TzUP = crypt.encryptLong(NOTE);
    }

    public String getinXEAIWqkta() {
        return inXEAIWqkta;
    }

    public String getbZI0mGySHpL() { return bZI0mGySHpL; }

    public String geteiz1WXHTZ3o() {
        return eiz1WXHTZ3o;
    }

    public String getlMIs9w2TzUP() {
        return lMIs9w2TzUP;
    }

    public String gibContent() {
        Crypt crypt = new Crypt();
        return crypt.decryptString(getbZI0mGySHpL());
    }

    public String gibAdapterPos() {
        return getinXEAIWqkta();
    }

    public long gibType() {
        Crypt crypt = new Crypt();
        return crypt.decryptLong(getlMIs9w2TzUP());
    }

    public long gibNoteColor() {
        Crypt crypt = new Crypt();
        return crypt.decryptLong(geteiz1WXHTZ3o());
    }


}
