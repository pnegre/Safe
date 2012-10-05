package com.pnegre.safe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import net.iharder.base64.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


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

    Secret(Secret s) {
        name = s.name;
        username = s.username;
        password = s.password;
        id = s.id;
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        Secret s = (Secret) o;
        return name.compareTo(s.name);
    }

    @Override
    public boolean equals(Object o) {
        Secret s = (Secret) o;
        return s.name.equals(this.name);
    }
}


interface Database {

    void destroy();

    boolean ready();

    List getSecrets();

    void newSecret(Secret s);

    Secret getSecret(int id);

    void deleteSecret(int id);

    void updateSecret(Secret s);

    void wipe();

}


/**
 * Implements the decorator pattern against SQLDatabase
 */
class EncryptedDatabase implements Database {

    private SimpleCrypt mCrypt;
    private boolean mIsReady;
    private Database cleanDatabase;

    class PasswordIncorrectException extends Exception { }

    EncryptedDatabase(Database db, Context ctx, String password, boolean force) throws PasswordIncorrectException {
        SQL2 sql2 = new SQL2(ctx);
        try {
            String storedHash = sql2.getPassword();
            String salt = sql2.getSalt();
            String saltedPassword = "";

            if (salt == null || storedHash == null || force) {
                salt = generateSalt();
                saltedPassword = salt + password;
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(saltedPassword.getBytes());
                String hash = Base64.encodeBytes(md.digest());
                sql2.savePassword(hash, salt);
            } else {
                saltedPassword = salt + password;
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(saltedPassword.getBytes());
                String hash = Base64.encodeBytes(md.digest());
                if (!storedHash.equals(hash)) throw new PasswordIncorrectException();
            }

            mCrypt = new SimpleCrypt(password.getBytes());
            mIsReady = true;
            cleanDatabase = db;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Fatal: Algorithm MD5 Not supported");
        } finally {
            sql2.close();
        }
    }

    private String generateSalt() {
        Random rnd = new Random();
        final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String allChars = AB + AB.toLowerCase() + "0123456789";
        StringBuilder sb = new StringBuilder(10);
        for (int i=0; i<10; i++) {
            sb.append(allChars.charAt(rnd.nextInt(allChars.length())));
        }
        return sb.toString();
    }


    @Override
    public void destroy() {
        mIsReady = false;
        mCrypt = null;
        cleanDatabase.destroy();
    }

    @Override
    public boolean ready() {
        return mIsReady;
    }

    @Override
    public List getSecrets() {
        List<Secret> slist = cleanDatabase.getSecrets();
        for (Secret s : slist)
            decryptSecret(s);

        Collections.sort(slist);
        return slist;

    }

    @Override
    public void newSecret(Secret s) {
        Secret ss = new Secret(s);
        encryptSecret(ss);
        cleanDatabase.newSecret(ss);
    }

    @Override
    public Secret getSecret(int id) {
        Secret s = cleanDatabase.getSecret(id);
        decryptSecret(s);
        return s;
    }

    @Override
    public void deleteSecret(int id) {
        cleanDatabase.deleteSecret(id);
    }

    @Override
    public void updateSecret(Secret s) {
        Secret ss = new Secret(s);
        encryptSecret(ss);
        cleanDatabase.updateSecret(ss);
    }

    @Override
    public void wipe() {
        cleanDatabase.wipe();
    }


    private void encryptSecret(Secret s) {
        s.name = cryptString(s.name);
        s.username = cryptString(s.username);
        s.password = cryptString(s.password);
    }

    private void decryptSecret(Secret s) {
        s.name = decryptString(s.name);
        s.username = decryptString(s.username);
        s.password = decryptString(s.password);
    }

    private String cryptString(String clear) {
        return Base64.encodeBytes(mCrypt.crypt(clear.getBytes()));
    }

    private String decryptString(String crypted) {
        try {
            byte[] raw = Base64.decode(crypted.getBytes());
            return new String(mCrypt.decrypt(raw));

        } catch (IOException e) {
            throw new RuntimeException();
        }

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


/**
 * Helper class. Used to save the user master password
 */
class SQL2 extends SQLiteOpenHelper {

    static final String DB_NAME = "safe_secret.db";
    static final int DB_VERSION = 5;

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
