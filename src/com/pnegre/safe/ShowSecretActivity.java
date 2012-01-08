package com.pnegre.safe;

import android.os.Bundle;
import android.app.Activity;


public class ShowSecretActivity extends Activity
{
	private SafeApp app;
	private Database database;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showsecret);
		app = (SafeApp) getApplication();
		database = app.database;
	}
}
