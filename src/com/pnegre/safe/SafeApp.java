package com.pnegre.safe;

import android.app.Application;

public class SafeApp extends Application
{
	private Database mDatabase;
	
	public static final String LOG_TAG = "SafeDefaultActivity";
	public static final String PASS_HIDE_STRING = "********";
	
	public void onCreate()
	{
		mDatabase = new DatabaseImp(this);
	}
	
	Database getDatabase()
	{
		return mDatabase;
	}
}
