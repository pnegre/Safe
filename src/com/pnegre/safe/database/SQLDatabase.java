package com.pnegre.safe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:45
 */

// SQLite android database
public class SQLDatabase extends SQLiteOpenHelper implements Database {
    static final String DB_NAME = "safe.db";
    static final int DB_VERSION = 4;

    // Constructor
    public SQLDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called only once, first time the DB is created
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table secret ( id integer primary key autoincrement, name text, username text, password text )";
        db.execSQL(sql);
    }

    // Called whenever newVersion != oldVersion
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists secret");
    }


    // Stores encrypted information on sql database
    public void newSecret(Secret secret) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.clear();
        values.put("name", secret.name);
        values.put("username", secret.username);
        values.put("password", secret.password);

        db.insertOrThrow("secret", null, values);
    }

    @Override
    public Secret getSecret(int id) {
        List<Secret> ss = getSecrets();
        for (Secret s : ss)
            if (s.id == id) return s;

        return null;
    }


    @Override
    public void destroy() {
        close();
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public List getSecrets() {
        SQLiteDatabase db = getReadableDatabase();
        List result = new ArrayList();
        Cursor cs = db.query("secret", null, null, null, null, null, null);
        cs.moveToFirst();
        while (cs.isAfterLast() == false) {
            int id = cs.getInt(0);
            String name = cs.getString(1);
            String username = cs.getString(2);
            String password = cs.getString(3);
            Secret s = new Secret(id, name, username, password);
            result.add(s);
            cs.moveToNext();
        }
        cs.close();
        return result;
    }

    public void deleteSecret(int id) {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from secret where id=" + String.valueOf(id));
    }

    @Override
    public void updateSecret(Secret s) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues values = new ContentValues();
        values.clear();
        values.put("name", s.name);
        values.put("username", s.username);
        values.put("password", s.password);
        db.update("secret", values, "id=" + s.id, null);
    }

    @Override
    public void wipe() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from secret");
    }

}
