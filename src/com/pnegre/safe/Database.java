package com.pnegre.safe;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


class Secret
{
	String name;
	String username;
	String password;
	int id;
	
	Secret(int i, String nm, String us, String pw)
	{
		name = nm;
		username = us;
		password = pw;
		id = i;
	}
	
	void decrypt(SimpleCrypt sc) throws Exception
	{
		name = sc.decrypt(name);
		username = sc.decrypt(username);
		password = sc.decrypt(username);
	}
	
	public String toString()
	{
		return name;
	}
}


class DatabaseException extends Exception {}


interface Database
{
	void init(String password);
	void destroy();
	boolean ready();
	Secret[] getSecrets() throws Exception;
	void newSecret(Secret s) throws Exception;
	Secret getSecret(int id) throws Exception;
	void deleteSecret(int id) throws Exception;
}


class DatabaseImp implements Database
{
	private SimpleCrypt sc = null;
	boolean isready = false;
	SQL sql;
	
	DatabaseImp(Context context)
	{
		sql = new SQL(context);
	}
	
	// Make sure that all is ready to go
	void assureReady() throws DatabaseException
	{
		if (isready == false) throw new DatabaseException();
	}
	
	public boolean ready()
	{
		return isready;
	}
	
	public void destroy()
	{
		sc = null;
		isready = false;
	}
	
	// Create the cipher object
	// Make sure that the password is correct (comparing with the stored hash in the database)
	public void init(String password)
	{
		try
		{
			sc = new SimpleCrypt(password);
			String pw = sql.getEncryptedPassword();
			if (pw == null)
				sql.storeEncryptedPassword(sc.crypt(password));
			else
				if (! sc.crypt(password).equals(pw)) throw new DatabaseException();
			
			isready = true;
		}
		catch (Exception e) 
		{
			Log.d(SafeApp.LOG_TAG, "Problem initializing encrypted database");
		}
	}
	
	public Secret[] getSecrets() throws Exception
	{
		assureReady();
		
		List<Secret> list = sql.getSecrets();
		
		for (Secret s : list) 
		{
			s.name = sc.decrypt(s.name);
			s.username = sc.decrypt(s.username);
			s.password = sc.decrypt(s.password);
		}
		
		
		int sz = list.size();
		Secret[] result = new Secret[sz];
		for(int j=0;j<sz;j++)
		{
			Secret s = (Secret) list.get(j);
			result[j] = s;
		}
		return result;
	}
	
	public void newSecret(Secret s) throws Exception
	{
		assureReady();
		sql.newSecret(sc.crypt(s.name), sc.crypt(s.username), sc.crypt(s.password));
	}
	
	public Secret getSecret(int id) throws Exception
	{
		assureReady();
		Secret ss[] = getSecrets();
		for (Secret s : ss)
		{
			if (s.id == id) return s;
		}
		return null;
	}
	
	public void deleteSecret(int id) throws Exception
	{
		assureReady();
		sql.deleteSecret(id);
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


