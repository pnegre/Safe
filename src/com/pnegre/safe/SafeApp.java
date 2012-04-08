package com.pnegre.safe;

import android.app.Application;


// TODO: Exportar XML+ZIP la base de dades encriptada
// TODO: Implementar canvi de password dins l'aplicació


public class SafeApp extends Application {
    private Database mDatabase;
    boolean showMenu = false;

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
}
