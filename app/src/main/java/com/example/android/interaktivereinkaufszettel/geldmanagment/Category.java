package com.example.android.interaktivereinkaufszettel.geldmanagment;

import com.example.android.interaktivereinkaufszettel.Crypt;

import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_PASSPHRASE;

public class Category {

    public static final long CATEGORY_SOLO_LIST = 0;
    public static final long CATEGORY_GROUP_LIST = 1;

    public static final String NAME      = "hLbs7G2mDon";
    public static final String TYPE      = "hw6Udm2FdSq";
    public static final String BESITZER  = "dp5nS2B6g8p";
    public static final String ID        = "dKd3dK7sqoC";

    private String hLbs7G2mDon; // Name
    private String hw6Udm2FdSq; // Typ
    private String dp5nS2B6g8p; // Besitzer
    private String dKd3dK7sqoC; // document ID

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
}
