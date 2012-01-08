package com.pnegre.safe;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;


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
		
		try
		{
			Bundle extras = getIntent().getExtras();
			int secretId = extras.getInt("secretid");
			Secret s = database.getSecret(secretId);
			TextView tname = (TextView) findViewById(R.id.secretsitename);
			tname.setText(s.name);
			TextView tusname = (TextView) findViewById(R.id.secretsiteusname);
			tusname.setText(s.username);
			TextView tpw = (TextView) findViewById(R.id.secretsitepw);
			tpw.setText(s.password);
		} 
		catch (Exception e) { }
	}
}
