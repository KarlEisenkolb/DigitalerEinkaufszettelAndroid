package com.example.android.interaktivereinkaufszettel.models_and_adapters;

import com.example.android.interaktivereinkaufszettel.security.Crypt;

import static com.example.android.interaktivereinkaufszettel.security.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.security.Crypt.CRYPT_USE_PASSPHRASE;

public class Category {

    public static final long CATEGORY_SOLO_LIST = 0;
    public static final long CATEGORY_GROUP_LIST = 1;

    public static final String NAME             = "hLbs7G2mDon";
    public static final String TYPE             = "hw6Udm2FdSq";
    public static final String BESITZER         = "dp5nS2B6g8p";
    public static final String ID               = "dKd3dK7sqoC";
    public static final String GESAMTSUMME      = "oTgsdFhklFl";
    public static final String BILLS_ANZAHL     = "pWhdLgkrkTs";
    public static final String DATUM_FIRSTBILL  = "pSbhElKUgLf";


    private String hLbs7G2mDon; // Name
    private String hw6Udm2FdSq; // Typ
    private String dp5nS2B6g8p; // Besitzer
    private String dKd3dK7sqoC; // document ID
    private String oTgsdFhklFl; // gesamtsumme aller rechnungen
    private String pWhdLgkrkTs; // anzahl rechnungen
    private String pSbhElKUgLf; // datum first rechnung

    public Category(){}

    public Category(String name, long type, String besitzer, String id){
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.hLbs7G2mDon = crypt.encryptString(name);
        this.hw6Udm2FdSq = crypt.encryptLong(type);
        this.dp5nS2B6g8p = crypt.encryptString(besitzer);
        this.dKd3dK7sqoC = crypt.encryptString(id);
    }

    public String gethLbs7G2mDon() { return hLbs7G2mDon; }
    public String gethw6Udm2FdSq() { return hw6Udm2FdSq; }
    public String getdp5nS2B6g8p() { return dp5nS2B6g8p; }
    public String getdKd3dK7sqoC() { return dKd3dK7sqoC; }
    public String getoTgsdFhklFl() { return oTgsdFhklFl; }
    public String getpWhdLgkrkTs() { return pWhdLgkrkTs; }
    public String getpSbhElKUgLf() { return pSbhElKUgLf; }

    public String gibName() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(gethLbs7G2mDon());
    }

    public long gibType() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(gethw6Udm2FdSq());
    }

    public String gibBesitzer() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getdp5nS2B6g8p());
    }

    public String gibId() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getdKd3dK7sqoC());
    }

    public long gibGesamtsumme() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(getoTgsdFhklFl());
    }

    public long gibAnzahlBills() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(getpWhdLgkrkTs());
    }

    public long gibDateOfFirstBill() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(getpSbhElKUgLf());
    }

    public void setzeName(String name) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.hLbs7G2mDon = crypt.encryptString(name);
    }

    public void setzeType(long type) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.hw6Udm2FdSq = crypt.encryptLong(type);
    }

    public void setzeBesitzer(String besitzer) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.dp5nS2B6g8p = crypt.encryptString(besitzer);
    }

    public void setzeId(String id) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.dKd3dK7sqoC = crypt.encryptString(id);
    }

    public void setzeGesamtsumme(long gesamtsumme) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.oTgsdFhklFl = crypt.encryptLong(gesamtsumme);
    }

    public void setzeAnzahlBills(long gesamtanzahl) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.pWhdLgkrkTs = crypt.encryptLong(gesamtanzahl);
    }

    public void setzeDateOfFirstBill(long firstdate) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.pSbhElKUgLf = crypt.encryptLong(firstdate);
    }
}
