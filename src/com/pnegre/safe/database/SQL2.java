package com.pnegre.safe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:44
 */
class SQL2 extends SQLiteOpenHelper {

    static final String DB_NAME = "safe_secret.db";
    static final int DB_VERSION = 6;

    // Constructor
    public SQL2(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called only once, first time the DB is created
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table user ( username text, cryptedpassword text, salt text )";
        db.execSQL(sql);
    }

    // Called whenever newVersion != oldVersion
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists user");
        onCreate(db);
    }

    void savePassword(String password, String salt) {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from user");

        ContentValues values = new ContentValues();
        values.clear();
        values.put("username", "user");
        values.put("cryptedpassword", password);
        values.put("salt", salt);
        db.insertOrThrow("user", null, values);
    }

    String getPassword() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cs = db.query("user", new String[]{"username", "cryptedpassword"},
                "username='user'", null, null, null, null);
        cs.moveToFirst();
        if (cs.isAfterLast()) {
            cs.close();
            db.close();
            return null;
        }
        String result = cs.getString(1);
        cs.close();
        db.close();

        return result;
    }

    String getSalt() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cs = db.query("user", new String[]{"username", "salt"},
                "username='user'", null, null, null, null);
        cs.moveToFirst();
        if (cs.isAfterLast()) {
            cs.close();
            db.close();
            return null;
        }
        String result = cs.getString(1);
        cs.close();
        db.close();

        return result;
    }
}
