package com.pnegre.safe;

import android.app.Application;


// TODO: Implementar "tags" o categories
// TODO: Poder editar tots els camps dels "secrets"
// TODO: Implementar canvi de password dins l'aplicació


public class SafeApp extends Application {
    private Database mDatabase;
    private boolean showMenu = false;
    String masterPassword = null;

    public static final String LOG_TAG = "SafeDefaultActivity";
    public static final String PASS_HIDE_STRING = "********";

    public void onCreate() {
        mDatabase = null;
    }

    void setDatabase(Database db) {
        mDatabase = db;
    }

    Database getDatabase() {
        return mDatabase;
    }

    boolean menuVisible() {
        return showMenu;
    }

    void setMenuVisibility(boolean yesno) {
        showMenu = yesno;
    }
}
