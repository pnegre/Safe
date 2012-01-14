package com.pnegre.safe;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.CheckBox;
import android.util.Log;

import android.app.AlertDialog;
import android.content.DialogInterface;



public class ShowSecretActivity extends Activity
{
	private boolean showPassword = false;
	private SafeApp app;
	private Database database;
	private Secret theSecret;
	private TextView tname;
	private TextView tusname;
	private TextView tpw;
	private Button butDel;
	private CheckBox cbShowpw;
	
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
			theSecret = database.getSecret(secretId);
			tname = (TextView) findViewById(R.id.secretsitename);
			tname.setText(theSecret.name);
			tusname = (TextView) findViewById(R.id.secretsiteusname);
			tusname.setText(theSecret.username);
			tpw = (TextView) findViewById(R.id.secretsitepw);
			showPasswordFlip();
			
			cbShowpw = (CheckBox) findViewById(R.id.checkpw);
			cbShowpw.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					showPasswordFlip();
				}
			});
			
			butDel = (Button) findViewById(R.id.butdelsecret);
			butDel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					showDeleteDialog();
				}
			});
		} 
		catch (Exception e) 
		{
			Log.d(SafeApp.LOG_TAG, "Exception in ShowSecretActivity.onCreate");
		}
	}
	
	// Mostra diàleg per esborrar el secret
	void showDeleteDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
		alert.setTitle("Are you sure?");
		
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				deleteSecret();
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {  
			}
		});
		
		alert.show();
	}
	
	// Esborra el secret
	void deleteSecret()
	{
		try
		{
			database.deleteSecret(theSecret.id);
		}
		catch (Exception e) 
		{
			Log.d(SafeApp.LOG_TAG, "Exception deleting secret");
		}
		finish();
	}
	
	// Mostra "XXXXXXXX" o la password, cada vegada que es crida la funció
	// O sigui, va alternant.
	void showPasswordFlip()
	{
		if (showPassword == false)
			tpw.setText(SafeApp.PASS_HIDE_STRING);
		else
			tpw.setText(theSecret.password);
		
		showPassword = ! showPassword;
	}
}
