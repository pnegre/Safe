package com.pnegre.safe;

import android.app.Application;

public class SafeApp extends Application
{
	Database database;
	
	public static final String LOG_TAG = "Safe";
	public static final String PASS_HIDE_STRING = "********";
	
	public void onCreate()
	{
		database = new DatabaseImp(this);
	}
}
