package com.example.android.interaktivereinkaufszettel.models_and_adapters;

import com.example.android.interaktivereinkaufszettel.security.Crypt;

import static com.example.android.interaktivereinkaufszettel.security.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.security.Crypt.CRYPT_USE_PASSPHRASE;

public class Nutzer {

    public static final String NAME             = "oPd6M2d8Hdg";
    public static final String ZAHLUNGSANTEIL   = "msH2g9Gw5mX";
    public static final String KONTOSTAND       = "psMv8H25dGp";
    public static final String ID               = "cW9v$lf6sMR";

    private String oPd6M2d8Hdg; // Name
    private String msH2g9Gw5mX; // Zahlungsanteil
    private String psMv8H25dGp; // Kontostand
    private String cW9v$lf6sMR; // document ID

    public Nutzer(){}

    public Nutzer(String name, Double zahlungsanteil, String id){
        Crypt cryptNormal = new Crypt(CRYPT_USE_DEFAULT_KEY);
        Crypt cryptPassphrase = new Crypt(CRYPT_USE_PASSPHRASE);
        this.oPd6M2d8Hdg = cryptNormal.encryptString(name);
        this.msH2g9Gw5mX = cryptPassphrase.encryptDouble(zahlungsanteil);
        this.psMv8H25dGp = cryptNormal.encryptDouble(0.0);
        this.cW9v$lf6sMR = cryptNormal.encryptString(id);
    }

    public String getoPd6M2d8Hdg() { return oPd6M2d8Hdg; }
    public String getMsH2g9Gw5mX() { return msH2g9Gw5mX; }
    public String getPsMv8H25dGp() { return psMv8H25dGp; }
    public String getcW9v$lf6sMR() { return cW9v$lf6sMR; }

    public String gibName() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getoPd6M2d8Hdg());
    }

    public Double gibZahlungsanteil() {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        return crypt.decryptDouble(getMsH2g9Gw5mX());
    }

    public Double gibKontostand() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptDouble(getPsMv8H25dGp());
    }

    public String gibId() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getcW9v$lf6sMR());
    }

    public void setzeName(String name) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.oPd6M2d8Hdg = crypt.encryptString(name);
    }

    public void setzeZahlungsanteil(Double gehalt) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.msH2g9Gw5mX = crypt.encryptDouble(gehalt);
    }

    public void setzeKontostand(Double kontostand) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.psMv8H25dGp = crypt.encryptDouble(kontostand);
    }

    public void setzeId(String id) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.cW9v$lf6sMR = crypt.encryptString(id);
    }
}
