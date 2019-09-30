package com.example.android.interaktivereinkaufszettel.geldmanagment;

import com.example.android.interaktivereinkaufszettel.Crypt;

import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_DEFAULT_KEY;
import static com.example.android.interaktivereinkaufszettel.Crypt.CRYPT_USE_PASSPHRASE;

public class Rechnung {

    static final String CONTENT     = "kd9G2nFs8Js";
    static final String KAUEFER     = "o8VsZ37Mdg6";
    static final String KATEGORIE   = "zF2mdPsV3j5";
    static final String PREIS       = "pY2md6KeutM";
    static final String DATUM       = "uB2ksp24bsP";
    static final String ID          = "sM43hs2G49n";

    private String kd9G2nFs8Js; // Content
    private String o8VsZ37Mdg6; // Kauefer
    private String zF2mdPsV3j5; // Kategorie
    private String pY2md6KeutM; // Preis
    private String uB2ksp24bsP; // Datum
    private String sM43hs2G49n; // Id

    public Rechnung(){}

    public Rechnung(String content, String kauefer, String kategorie, Double preis, long datum, String id){
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.kd9G2nFs8Js = crypt.encryptString(content);
        this.o8VsZ37Mdg6 = crypt.encryptString(kauefer);
        this.zF2mdPsV3j5 = crypt.encryptString(kategorie);
        this.pY2md6KeutM = crypt.encryptDouble(preis);
        this.uB2ksp24bsP = crypt.encryptLong(datum);
        this.sM43hs2G49n = crypt.encryptString(id);
    }

    public String getkd9G2nFs8Js() { return kd9G2nFs8Js; }
    public String geto8VsZ37Mdg6() { return o8VsZ37Mdg6; }
    public String getzF2mdPsV3j5() { return zF2mdPsV3j5; }
    public String getpY2md6KeutM() { return pY2md6KeutM; }
    public String getuB2ksp24bsP() { return uB2ksp24bsP; }
    public String getsM43hs2G49n() { return sM43hs2G49n; }

    public String gibContent() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getkd9G2nFs8Js());
    }

    public String gibKauefer() {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        return crypt.decryptString(geto8VsZ37Mdg6());
    }

    public String gibKategorie() {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        return crypt.decryptString(getzF2mdPsV3j5());
    }

    public Double gibPreis() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptDouble(getpY2md6KeutM());
    }

    public long gibDatum() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(getuB2ksp24bsP());
    }

    public String gibId() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getsM43hs2G49n());
    }

    public void setzeContent(String content) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.kd9G2nFs8Js = crypt.encryptString(content);
    }

    public void setzeKauefer(String kauefer) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.o8VsZ37Mdg6 = crypt.encryptString(kauefer);
    }

    public void setzeKategorie(String kategorie) {
        Crypt crypt = new Crypt(CRYPT_USE_PASSPHRASE);
        this.zF2mdPsV3j5 = crypt.encryptString(kategorie);
    }

    public void setzePreis(Double preis) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.pY2md6KeutM = crypt.encryptDouble(preis);
    }

    public void setzeDatum(long datum) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.uB2ksp24bsP = crypt.encryptLong(datum);
    }

    public void setzeId(String id) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.sM43hs2G49n = crypt.encryptString(id);
    }
}
