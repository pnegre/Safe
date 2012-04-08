package com.pnegre.safe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import net.iharder.base64.Base64;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class Secret implements Comparable {
    String name;
    String username;
    String password;
    int id;

    Secret(int id, String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.id = id;
    }

    public String toString() {
        return name;
    }

    public int compareTo(Object o) {
        Secret s = (Secret) o;
        return name.compareTo(s.name);
    }
}


class DatabaseException extends Exception {
}


interface Database {

    void destroy();
    boolean ready();
    List getSecrets() throws Exception;
    void newSecret(Secret s) throws Exception;
    Secret getSecret(int id) throws Exception;
    void deleteSecret(int id) throws Exception;

}


/**
 * Implements the decorator pattern against SQLDatabase
 */
class EncryptedDatabase implements Database {

    private SimpleCrypt mCrypt;
    private boolean mIsReady;
    private Database cleanDatabase;

    EncryptedDatabase(Database db, Context ctx, String password) {
        try {
            SQL2 sql2 = new SQL2(ctx);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            String userPw = Base64.encodeBytes(md.digest());
            String storedPw = sql2.getPassword();

            if (storedPw == null)
                sql2.savePassword(userPw);
            else if (!userPw.equals(storedPw)) throw new DatabaseException();

            mCrypt = new SimpleCrypt(password.getBytes());
            mIsReady = true;
            cleanDatabase = db;

        } catch (Exception e) {
            Log.d(SafeApp.LOG_TAG, "Problem initializing encrypted database");
            e.printStackTrace();
        }
    }


    @Override
    public void destroy() {
        mIsReady = false;
        mCrypt = null;
    }

    @Override
    public boolean ready() {
        return mIsReady;
    }

    @Override
    public List getSecrets() throws Exception {
        List<Secret> slist = cleanDatabase.getSecrets();
        for (Secret s : slist)
            decryptSecret(s);

        Collections.sort(slist);
        return slist;

    }

    @Override
    public void newSecret(Secret s) throws Exception {
        encryptSecret(s);
        cleanDatabase.newSecret(s);
    }

    @Override
    public Secret getSecret(int id) throws Exception {
        Secret s = cleanDatabase.getSecret(id);
        decryptSecret(s);
        return s;
    }

    @Override
    public void deleteSecret(int id) throws Exception {
        cleanDatabase.deleteSecret(id);
    }


    private void encryptSecret(Secret s) throws Exception {
        s.name = cryptString(s.name);
        s.username = cryptString(s.username);
        s.password = cryptString(s.password);
    }

    private void decryptSecret(Secret s) throws Exception {
        s.name = decryptString(s.name);
        s.username = decryptString(s.username);
        s.password = decryptString(s.password);
    }

    private String cryptString(String clear) throws Exception {
        return Base64.encodeBytes(mCrypt.crypt(clear.getBytes()));
    }

    private String decryptString(String crypted) throws Exception {
        byte[] raw = Base64.decode(crypted.getBytes());
        return new String(mCrypt.decrypt(raw));
    }


}


// SQLite android database
class SQLDatabase extends SQLiteOpenHelper implements Database {
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

    public void deleteAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from secret");
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
    public Secret getSecret(int id) throws Exception {
        List<Secret> ss = getSecrets();
        for (Secret s : ss)
            if (s.id == id) return s;

        return null;
    }


    @Override
    public void destroy() {
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
        return result;
    }

    public void deleteSecret(int id) {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from secret where id=" + String.valueOf(id));
    }

}


/**
 * Helper class. Used to save the user master password
 */
class SQL2 extends SQLiteOpenHelper {

    static final String DB_NAME = "safe_secret.db";
    static final int DB_VERSION = 4;

    // Constructor
    public SQL2(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called only once, first time the DB is created
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table user ( username text, cryptedpassword text )";
        db.execSQL(sql);
    }

    // Called whenever newVersion != oldVersion
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists user");
        onCreate(db);
    }

    void savePassword(String password) {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from user");

        ContentValues values = new ContentValues();
        values.clear();
        values.put("username", "user");
        values.put("cryptedpassword", password);
        db.insertOrThrow("user", null, values);
    }

    String getPassword() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cs = db.query("user", new String[]{"username", "cryptedpassword"},
                "username='user'", null, null, null, null);
        cs.moveToFirst();
        if (cs.isAfterLast()) return null;

        return cs.getString(1);
    }
}
