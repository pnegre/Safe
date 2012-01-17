package com.pnegre.safe;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

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
	void init(String password);
	void destroy();
	boolean ready();
	List getSecrets() throws Exception;
	void newSecret(Secret s) throws Exception;
	Secret getSecret(int id) throws Exception;
	void deleteSecret(int id) throws Exception;
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
	void assureReady() throws DatabaseException
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
	
	// Create the cipher object
	// Make sure that the password is correct (comparing with the stored hash in the database)
	public void init(String password)
	{
		try
		{
			mCrypt = new SimpleCrypt(password);
			String pw = mSQL.getEncryptedPassword();
			if (pw == null)
				mSQL.storeEncryptedPassword(mCrypt.crypt(password));
			else
				if (! mCrypt.crypt(password).equals(pw)) throw new DatabaseException();
			
			mIsReady = true;
		}
		catch (Exception e) 
		{
			Log.d(SafeApp.LOG_TAG, "Problem initializing encrypted database");
		}
	}
	
	public List getSecrets() throws Exception
	{
		assureReady();
		List<Secret> list = mSQL.getSecrets();
		for (Secret s : list) 
		{
			s.name = mCrypt.decrypt(s.name);
			s.username = mCrypt.decrypt(s.username);
			s.password = mCrypt.decrypt(s.password);
		}
		Collections.sort(list);
		
		return list;
	}
	
	public void newSecret(Secret s) throws Exception
	{
		assureReady();
		mSQL.newSecret(mCrypt.crypt(s.name), mCrypt.crypt(s.username), mCrypt.crypt(s.password));
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
	static final String TAG = "Database";
	static final String DB_NAME = "safe.db";
	static final int DB_VERSION = 2;

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
		sql = "create table setup ( cryptedpassword text )";
		db.execSQL(sql);
	}

	// Called whenever newVersion != oldVersion
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("drop table if exists secret");
		db.execSQL("drop table if exists setup");
		onCreate(db);
	}

	public void deleteAllData()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("delete from secret");
		db.execSQL("delete from setup");
	}
	
	// Stores encrypted information on sql database
	public void newSecret(String encName, String encUsername, String encPassword)
	{
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.clear();
		values.put("name", encName);
		values.put("username", encUsername);
		values.put("password", encPassword);
		
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
		Cursor cs = db.query("setup", null, null, null, null, null, null);
		cs.moveToFirst();
		if (cs.isAfterLast() == true) return null;
		
		return cs.getString(0);
	}
	
	public void storeEncryptedPassword(String encryptedPassword)
	{
		SQLiteDatabase db = getReadableDatabase();
		db.execSQL("delete from setup");
		
		ContentValues values = new ContentValues();
		values.clear();
		values.put("cryptedpassword", encryptedPassword);
		db.insertOrThrow("setup", null, values);
	}
}


