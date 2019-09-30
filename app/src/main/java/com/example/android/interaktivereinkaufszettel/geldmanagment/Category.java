package com.example.android.interaktivereinkaufszettel.geldmanagment;

import com.example.android.interaktivereinkaufszettel.Crypt;

import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_PASSPHRASE;

public class Category {

    static final String NAME      = "oPd6M2d8Hdg";
    static final String TYPE      = "msH2g9Gw5mX";
    static final String BESITZER  = "psMv8H25dGp";
    static final String ID        = "cW9v$lf6sMR";

    private String oPd6M2d8Hdg; // Name
    private String msH2g9Gw5mX; // Typ
    private String psMv8H25dGp; // Besitzer
    private String cW9v$lf6sMR; // document ID

    public Category(){}

    public Category(String name, long type, String besitzer, String id){
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.oPd6M2d8Hdg = crypt.encryptString(name);
        this.msH2g9Gw5mX = crypt.encryptLong(type);
        this.psMv8H25dGp = crypt.encryptString(besitzer);
        this.cW9v$lf6sMR = crypt.encryptString(id);
    }

    public String getoPd6M2d8Hdg() { return oPd6M2d8Hdg; }
    public String getMsH2g9Gw5mX() { return msH2g9Gw5mX; }
    public String getPsMv8H25dGp() { return psMv8H25dGp; }
    public String getcW9v$lf6sMR() { return cW9v$lf6sMR; }

    public String gibName() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getoPd6M2d8Hdg());
    }

    public long gibType() {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        return crypt.decryptLong(getMsH2g9Gw5mX());
    }

    public String gibBesitzer() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getPsMv8H25dGp());
    }

    public String gibId() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getcW9v$lf6sMR());
    }

    public void setzeName(String name) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.oPd6M2d8Hdg = crypt.encryptString(name);
    }

    public void setzeType(long type) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.msH2g9Gw5mX = crypt.encryptLong(type);
    }

    public void setzeBesitzer(String besitzer) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.psMv8H25dGp = crypt.encryptString(besitzer);
    }

    public void setzeId(String id) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.cW9v$lf6sMR = crypt.encryptString(id);
    }
}
