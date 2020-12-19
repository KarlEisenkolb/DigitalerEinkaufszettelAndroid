package com.example.android.interaktivereinkaufszettel.ModelsAndAdapters;

import com.example.android.interaktivereinkaufszettel.Security.Crypt;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.interaktivereinkaufszettel.Security.Crypt.CRYPT_USE_DEFAULT_KEY;

public class Rechnung {

    public static final long RECHNUNG_GEKAUFT = 0;
    public static final long RECHNUNG_GEPLANT = 1;
    public static final long RECHNUNG_ZAHLUNG = 2;

    public static final String CONTENT                  = "kd9G2nFs8Js";
    public static final String KAUEFER                  = "o8VsZ37Mdg6";
    public static final String KATEGORIE                = "zF2mdPsV3j5";
    public static final String PREIS                    = "pY2md6KeutM";
    public static final String NUTZERLISTE              = "jEYndDjdkHs";
    public static final String NUTZERZAHLUNGSANTEILE    = "lFsWgdHfgSn";
    public static final String DATUM                    = "uB2ksp24bsP";
    public static final String TYPE                     = "fiJ7Dn2m63d";
    public static final String ID                       = "sM43hs2G49n";

    private String kd9G2nFs8Js;         // Content
    private String o8VsZ37Mdg6;         // Kauefer
    private String zF2mdPsV3j5;         // Kategorie
    private String pY2md6KeutM;         // Preis
    private List<String> jEYndDjdkHs = new ArrayList<>();   // NutzerListe
    private List<String> lFsWgdHfgSn = new ArrayList<>();   // NutzerZahlungsanteile
    private long   uB2ksp24bsP;         // Datum
    private String fiJ7Dn2m63d;         // Type
    private String sM43hs2G49n;         // Id

    public Rechnung(){}

    public Rechnung(String content,
                    String kauefer,
                    List<Nutzer> nutzerList,
                    String kategorie,
                    Double preis,
                    long datum,
                    long type,
                    String id){
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.kd9G2nFs8Js = crypt.encryptString(content);
        this.o8VsZ37Mdg6 = crypt.encryptString(kauefer);

        for (Nutzer nutzer : nutzerList){
            this.jEYndDjdkHs.add(crypt.encryptString(nutzer.gibName()));
            this.lFsWgdHfgSn.add(crypt.encryptDouble(nutzer.gibZahlungsanteil()));
        }

        this.zF2mdPsV3j5 = crypt.encryptString(kategorie);
        this.pY2md6KeutM = crypt.encryptDouble(preis);
        this.uB2ksp24bsP = datum;
        this.fiJ7Dn2m63d = crypt.encryptLong(type);
        this.sM43hs2G49n = crypt.encryptString(id);
    }

    public String getkd9G2nFs8Js() { return kd9G2nFs8Js; }
    public String geto8VsZ37Mdg6() { return o8VsZ37Mdg6; }
    public String getzF2mdPsV3j5() { return zF2mdPsV3j5; }
    public String getpY2md6KeutM() { return pY2md6KeutM; }
    public List<String> getjEYndDjdkHs() { return jEYndDjdkHs; }
    public List<String> getlFsWgdHfgSn() { return lFsWgdHfgSn; }
    public long   getuB2ksp24bsP() { return uB2ksp24bsP; }
    public String getfiJ7Dn2m63d() { return fiJ7Dn2m63d; }
    public String getsM43hs2G49n() { return sM43hs2G49n; }

    public String gibContent() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getkd9G2nFs8Js());
    }

    public String gibKauefer() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(geto8VsZ37Mdg6());
    }

    public String gibKategorie() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptString(getzF2mdPsV3j5());
    }

    public Double gibPreis() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptDouble(getpY2md6KeutM());
    }

    public List<String> gibNutzerListe() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        List<String> nutzerListDecrypt = new ArrayList<>();
        for (String nutzerCrypt : getjEYndDjdkHs())
            nutzerListDecrypt.add(crypt.decryptString(nutzerCrypt));
        return nutzerListDecrypt;
    }

    public List<Double> gibNutzerZahlungsanteile() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        List<Double> anteilListDecrypt = new ArrayList<>();
        for (String anteilCrypt : getlFsWgdHfgSn())
            anteilListDecrypt.add(crypt.decryptDouble(anteilCrypt));
        return anteilListDecrypt;
    }

    public long gibDatum() {
        return getuB2ksp24bsP();
    }

    public long gibType() {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        return crypt.decryptLong(getfiJ7Dn2m63d());
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
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.o8VsZ37Mdg6 = crypt.encryptString(kauefer);
    }

    public void setzeKategorie(String kategorie) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.zF2mdPsV3j5 = crypt.encryptString(kategorie);
    }

    public void setzePreis(Double preis) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.pY2md6KeutM = crypt.encryptDouble(preis);
    }

    public void setzeDatum(long datum) {
        this.uB2ksp24bsP = datum;
    }

    public void setzeType(long type) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.fiJ7Dn2m63d = crypt.encryptLong(type);
    }

    public void setzeId(String id) {
        Crypt crypt = new Crypt(CRYPT_USE_DEFAULT_KEY);
        this.sM43hs2G49n = crypt.encryptString(id);
    }
}
