package com.pnegre.safe;

import android.app.Application;


// TODO: Implementar "tags" o categories
// TODO: Poder editar els secrets des de l'activitat ShowSecretActivity
// TODO: Exportar XML+ZIP la base de dades encriptada
// TODO: Implementar canvi de password dins l'aplicació


public class SafeApp extends Application {
    private Database mDatabase;
    private boolean showMenu = false;

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
