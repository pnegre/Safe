package com.pnegre.safe;

import android.os.Bundle;
import android.app.Activity;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.util.Log;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class NewSecretActivity extends Activity
{
	private EditText mETsitename, mETusername, mETpassword;
	private Button mBTnewsecret;
	private SafeApp mApp;
	private Database mDatabase;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newsecret);
		mApp = (SafeApp) getApplication();
		mDatabase = mApp.getDatabase();
		
		mETsitename = (EditText) findViewById(R.id.sitename);
		mETusername = (EditText) findViewById(R.id.siteusname);
		mETpassword = (EditText) findViewById(R.id.sitepassword);
		
		mBTnewsecret = (Button) findViewById(R.id.butnewsecret);
		mBTnewsecret.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				newSecret();
			}
		});
	}
	
	void newSecret()
	{
		String sname = mETsitename.getText().toString();
		String usname = mETusername.getText().toString();
		String pw = mETpassword.getText().toString();
		if (sname.equals("") || pw.equals(""))
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
			alert.setTitle("No Blanks!");
			alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int whichButton) { }
			});
			alert.show();
		}
		else
		{
			try
			{
				Secret s = new Secret(0,sname,usname,pw);
				mDatabase.newSecret(s);
				finish();
			} 
			catch (Exception e) 
			{
				Log.d(SafeApp.LOG_TAG, "Error when inserting new secret"); 
			}
		}
	}
}
