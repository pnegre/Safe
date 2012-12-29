package com.pnegre.safe;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.pnegre.safe.database.Database;


// TODO: Implementar "tags" o categories


public class SafeApp extends Application {
    private Database mDatabase;
    String masterPassword = null;

    public static final String LOG_TAG = "SafeMainActivity";
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

    public static void initMainActivity(Context ctx) {
        Intent i = new Intent(ctx, InitialActivity.class);
        ctx.startActivity(i);
    }
}
