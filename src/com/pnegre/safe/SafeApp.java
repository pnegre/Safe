package com.pnegre.safe;

import android.app.Application;

public class SafeApp extends Application
{
	Database database;
	
	public void onCreate()
	{
		database = new DatabaseImp();
	}
}
