package com.example.android.interaktivereinkaufszettel;

import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;

public class Note {

    static final long NOTE_NO_COLOR = 0;
    static final long NOTE_COLOR_GREEN = 1;
    static final long NOTE_COLOR_YELLOW = 2;
    static final long NOTE = 0;
    static final long CATEGORY = 1;

    static final String ADAPTER_POS = "inXEAIWqkta";
    static final String CONTENT     = "bZI0mGySHpL";
    static final String NOTE_COLOR  = "eiz1WXHTZ3o";
    static final String TYPE        = "lMIs9w2TzUP";
    static final String ID          = "sMIosTjdmzc";

    private String bZI0mGySHpL; //content
    private String inXEAIWqkta; //adapterPos
    private String eiz1WXHTZ3o; //noteColor
    private String lMIs9w2TzUP; //type
    private String sMIosTjdmzc; //document ID

    public Note() {
    }

    public Note(String content, String adapterPos, long noteColor, long type, String documentId) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.bZI0mGySHpL = crypt.encryptString(content);
        this.inXEAIWqkta = adapterPos;
        this.eiz1WXHTZ3o = crypt.encryptLong(noteColor);
        this.lMIs9w2TzUP = crypt.encryptLong(type);
        this.sMIosTjdmzc = crypt.encryptString(documentId);
    }

    public Note(String content, String adapterPos, String documentId) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.bZI0mGySHpL = crypt.encryptString(content);
        this.inXEAIWqkta = adapterPos;
        this.eiz1WXHTZ3o = crypt.encryptLong(NOTE_NO_COLOR);
        this.lMIs9w2TzUP = crypt.encryptLong(NOTE);
        this.sMIosTjdmzc = crypt.encryptString(documentId);
    }

    public String getinXEAIWqkta() {
        return inXEAIWqkta;
    }
    public String getbZI0mGySHpL() { return bZI0mGySHpL; }
    public String geteiz1WXHTZ3o() {
        return eiz1WXHTZ3o;
    }
    public String getlMIs9w2TzUP() { return lMIs9w2TzUP; }
    public String getsMIosTjdmzc() { return sMIosTjdmzc; }

    public String gibContent() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getbZI0mGySHpL());
    }

    public String gibAdapterPos() {
        return getinXEAIWqkta();
    }

    public long gibType() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(getlMIs9w2TzUP());
    }

    public long gibNoteColor() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(geteiz1WXHTZ3o());
    }

    public String gibId() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getsMIosTjdmzc());
    }

    public void setzeId(String documentId) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.sMIosTjdmzc = crypt.encryptString(documentId);
    }

    public void setzeColor(long noteColor) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.eiz1WXHTZ3o = crypt.encryptLong(noteColor);
    }

}
