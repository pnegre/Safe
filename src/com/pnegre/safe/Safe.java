package com.pnegre.safe;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.util.Log;

import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Intent;


public class Safe extends ListActivity
{
	private SafeApp app;
	private Database database;
	private boolean showingDialog = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		app = (SafeApp) getApplication();
		database = app.getDatabase();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		database.destroy();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if (showingDialog) return;
		
		if (database.ready() == false)
			showMasterPwDialog();
		else
			setAdapter();
	}
	
	void showMasterPwDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
		alert.setTitle("Password");
		// Set an EditText view to get user input   
		final EditText input = new EditText(this);
		input.setTransformationMethod(new android.text.method.PasswordTransformationMethod().getInstance());

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				String pw = input.getText().toString();
				database.init(pw);
				if (database.ready()) {
					setAdapter();
					showingDialog = false;
				}
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {  
				showingDialog = false;
			}
		});
 
		alert.setView(input);
		alert.show();
		showingDialog = true;
	}

	void setAdapter()
	{
		try
		{
			List<Secret> secrets = database.getSecrets();
			Secret[] secretsArray = new Secret[secrets.size()];
			secrets.toArray(secretsArray);
			ArrayAdapter<Secret> adapter = new ArrayAdapter<Secret>(this, android.R.layout.simple_list_item_1, secretsArray);
			setListAdapter(adapter);
		}
		catch (Exception e) 
		{
			Log.d(SafeApp.LOG_TAG, "Problem in setAdapter (Safe class)"); 
		}
	}
	
	void newSecret()
	{
		if (!database.ready()) return;
		
		Intent i = new Intent(this, NewSecretActivity.class);
		startActivity(i);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		Secret item = (Secret) getListAdapter().getItem(position);
		int secretId = item.id;
		Intent i = new Intent(this, ShowSecretActivity.class);
		i.putExtra("secretid",secretId);
		startActivity(i);
	}
	
	
	// Inflate res/menu/mainmenu.xml
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}


	// Respond to user click on menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) 
		{
		case R.id.masterpw:
			if (!database.ready())
				showMasterPwDialog();
			return true;

		case R.id.newsecret:
			newSecret();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
