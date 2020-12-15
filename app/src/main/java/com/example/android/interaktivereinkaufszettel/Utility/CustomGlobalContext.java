package com.example.android.interaktivereinkaufszettel.Utility;

import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Nutzer;

import java.util.ArrayList;
import java.util.List;

public class CustomGlobalContext {

    private static CustomGlobalContext customGlobalContext;
    private List<Nutzer> nutzerList = new ArrayList<>();

    public static CustomGlobalContext getInstance(){
        if (customGlobalContext == null)
            customGlobalContext = new CustomGlobalContext();
        return customGlobalContext;
    }

    public void firestoreSynchronizeNutzerList(){

    }

    public List<Nutzer> getNutzerList(){
        return this.nutzerList;
    }
}
