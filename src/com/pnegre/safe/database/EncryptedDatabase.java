package com.pnegre.safe.database;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:44
 */

import android.content.Context;
import com.pnegre.simplecrypt.SimpleCrypt;
import net.iharder.base64.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Implements the decorator pattern against SQLDatabase
 */
public class EncryptedDatabase implements Database {

    private SimpleCrypt mCrypt;
    private boolean mIsReady;
    private Database cleanDatabase;

    public class PasswordIncorrectException extends Exception { }

    public EncryptedDatabase(Database db, Context ctx, String password, boolean force) throws PasswordIncorrectException {
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