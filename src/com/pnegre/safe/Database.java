package com.pnegre.safe;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.security.MessageDigest;

import java.util.*;


class Secret implements Comparable
{
	String name;
	String username;
	String password;
	int id;
	
	Secret(int id_, String name_, String username_, String password_)
	{
		name = name_;
		username = username_;
		password = password_;
		id = id_;
	}
	
	public String toString()
	{
		return name;
	}
	
	public int compareTo(Object o)
	{
		Secret s = (Secret) o;
		return name.compareTo(s.name);
	}
}


class DatabaseException extends Exception {}


interface Database
{
	void    init(String password);
	void    destroy();
	boolean ready();
	List    getSecrets() throws Exception;
	void    newSecret(Secret s) throws Exception;
	Secret  getSecret(int id) throws Exception;
	void    deleteSecret(int id) throws Exception;
}


class DatabaseImp implements Database
{
	private SimpleCrypt mCrypt = null;
	boolean mIsReady = false;
	SQL mSQL;
	
	DatabaseImp(Context context)
	{
		mSQL = new SQL(context);
	}
	
	// Make sure that all is ready to go
	private void assureReady() throws Exception
	{
		if (mIsReady == false) throw new DatabaseException();
	}
	
	public boolean ready()
	{
		return mIsReady;
	}
	
	public void destroy()
	{
		mCrypt = null;
		mIsReady = false;
	}
	
	// Create the SimpleCrypt object
	// Make sure that the password is correct (comparing with the stored hash in the database)
	public void init(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(password.getBytes());
			String userPw = Base64.encodeBytes(md.digest());
			String storedPw = mSQL.getEncryptedPassword();
			
			if (storedPw == null)
				mSQL.storeEncryptedPassword(userPw);
			else
				if (! userPw.equals(storedPw)) throw new DatabaseException();
			
			mCrypt = new SimpleCrypt(password.getBytes());
			mIsReady = true;
		}
		catch (Exception e) 
		{
			Log.d(SafeApp.LOG_TAG, "Problem initializing encrypted database");
			e.printStackTrace();
		}
	}
	
	private void encryptSecret(Secret s) throws Exception
	{
		s.name = cryptString(s.name);
		s.username = cryptString(s.username);
		s.password = cryptString(s.password);
	}
	
	private void decryptSecret(Secret s) throws Exception
	{
		s.name = decryptString(s.name);
		s.username = decryptString(s.username);
		s.password = decryptString(s.password);
	}
	
	private String cryptString(String clear) throws Exception
	{
		return Base64.encodeBytes(mCrypt.crypt(clear.getBytes()));
	}
	
	private String decryptString(String crypted) throws Exception
	{
		byte[] raw = Base64.decode(crypted.getBytes());
		String clear = new String(mCrypt.decrypt(raw));
		return clear;
	}
	
	public List getSecrets() throws Exception
	{
		assureReady();
		List<Secret> slist = mSQL.getSecrets();
		for (Secret s : slist) 
			decryptSecret(s);
		
		Collections.sort(slist);
		
		return slist;
	}
	
	public void newSecret(Secret s) throws Exception
	{
		assureReady();
		encryptSecret(s);
		mSQL.newSecret(s);
	}
	
	public Secret getSecret(int id) throws Exception
	{
		assureReady();
		List<Secret> ss = getSecrets();
		for (Secret s : ss)
			if (s.id == id) return s;
		
		return null;
	}
	
	public void deleteSecret(int id) throws Exception
	{
		assureReady();
		mSQL.deleteSecret(id);
	}
}




// SQLite android database
class SQL extends SQLiteOpenHelper
{
	static final String DB_NAME = "safe.db";
	static final int DB_VERSION = 3;

	// Constructor
	public SQL(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	// Called only once, first time the DB is created
	public void onCreate(SQLiteDatabase db)
	{
		String sql = "create table secret ( id integer primary key autoincrement, name text, username text, password text )";
		db.execSQL(sql);
		sql = "create table user ( username text, cryptedpassword text )";
		db.execSQL(sql);
	}

	// Called whenever newVersion != oldVersion
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("drop table if exists secret");
		db.execSQL("drop table if exists user");
		onCreate(db);
	}

	public void deleteAllData()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("delete from secret");
		db.execSQL("delete from user");
	}
	
	// Stores encrypted information on sql database
	public void newSecret(Secret secret)
	{
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.clear();
		values.put("name", secret.name);
		values.put("username", secret.username);
		values.put("password", secret.password);
		
		db.insertOrThrow("secret", null, values);
	}
	
	public List getSecrets()
	{
		SQLiteDatabase db = getReadableDatabase();
		List result = new ArrayList();
		Cursor cs = db.query("secret", null, null, null, null, null, null);
		cs.moveToFirst();
		while (cs.isAfterLast() == false)
		{
			int id = cs.getInt(0);
			String name = cs.getString(1);
			String username = cs.getString(2);
			String password = cs.getString(3);
			Secret s = new Secret(id,name,username,password);
			result.add(s);
			cs.moveToNext();
		}
		return result;
	}
	
	void deleteSecret(int id)
	{
		SQLiteDatabase db = getReadableDatabase();
		db.execSQL("delete from secret where id=" + String.valueOf(id));
	}
	
	public String getEncryptedPassword()
	{
		SQLiteDatabase db = getReadableDatabase();
		Cursor cs = db.query("user", new String[] { "username", "cryptedpassword" },
			"username='user'", null, null, null, null);
		cs.moveToFirst();
		if (cs.isAfterLast() == true) return null;
		
		return cs.getString(1);
	}
	
	public void storeEncryptedPassword(String encryptedPassword)
	{
		SQLiteDatabase db = getReadableDatabase();
		db.execSQL("delete from user");
		
		ContentValues values = new ContentValues();
		values.clear();
		values.put("username","user");
		values.put("cryptedpassword", encryptedPassword);
		db.insertOrThrow("user", null, values);
	}
}


