package com.pnegre.safe;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;


class Secret
{
	String name;
	String username;
	String password;
	
	Secret(String nm, String us, String pw)
	{
		name = nm;
		username = us;
		password = pw;
	}
	
	public String toString()
	{
		return name;
	}
}




interface Database
{
	void init(String password);
	void destroy();
	boolean ready();
	Secret[] getSecrets();
	void newSecret(Secret s);
}


class DatabaseImp implements Database
{
	private SimpleCrypt sc = null;
	boolean isready = false;
	
	public boolean ready()
	{
		return isready;
	}
	
	public void destroy()
	{
		sc = null;
		isready = false;
	}
	
	public void init(String password)
	{
		try
		{
			sc = new SimpleCrypt(password);
			isready = true;
		} 
		catch (Exception e) { }
	}
	
	public Secret[] getSecrets()
	{
		if (isready == false) return null;
		
		Secret[] ss = { 
			new Secret("meneame.net","us1","pw1"), 
			new Secret("slashdot.org","abc","dfg"),
			new Secret("Barrapunto.org","paba","mego")
		};
		return ss;
	}
	
	public void newSecret(Secret s)
	{
	}
}




// SQLite android database
class SQL extends SQLiteOpenHelper
{
	static final String TAG = "Database";
	static final String DB_NAME = "safe.db";
	static final int DB_VERSION = 1;

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
	}

	// Called whenever newVersion != oldVersion
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("drop table if exists secret");
		onCreate(db);
	}

	public void deleteAllData()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("delete from secret");
	}
	
	private void newSecret(Secret s)
	{
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.clear();
		values.put("name", s.name);
		values.put("username", s.username);
		values.put("password", s.password);
		
		db.insertOrThrow("secret", null, values);
	}
}


