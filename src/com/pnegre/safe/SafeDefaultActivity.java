package com.pnegre.safe;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.util.Log;

import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Intent;
import android.widget.Button;


public class SafeDefaultActivity extends ListActivity
{
	private SafeApp   mApp;
	private Database  mDatabase;
	private boolean   mShowingDialog = false;
	private ViewGroup mHeader;
	private Button    mBTmasterSecret;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mApp = (SafeApp) getApplication();
		mDatabase = mApp.getDatabase();
		ListView lv = getListView();
		LayoutInflater inflater = getLayoutInflater();
		mHeader = (ViewGroup)inflater.inflate(R.layout.header, lv, false);
		lv.addHeaderView(mHeader, null, false);
		setListAdapter(null);
		
		mBTmasterSecret = (Button) findViewById(R.id.butmastersecret);
		mBTmasterSecret.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showMasterPwDialog();
			}
		});
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mDatabase.destroy();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if (mShowingDialog) return;
		
		if (mDatabase.ready() == true)
			setAdapter();
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
		case R.id.changepw:
			if (!mDatabase.ready())
				return true;
			
			// TODO: aquí codi per canviar la master password
			
			return true;

		case R.id.newsecret:
			newSecret();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
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
	
	
	private void showMasterPwDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
		alert.setTitle("Password");
		// Set an EditText view to get user input   
		final EditText input = new EditText(this);
		input.setTransformationMethod(new android.text.method.PasswordTransformationMethod().getInstance());

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				String pw = input.getText().toString();
				mDatabase.init(pw);
				if (mDatabase.ready()) {
					setAdapter();
					mShowingDialog = false;
				}
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {  
				mShowingDialog = false;
			}
		});
 
		alert.setView(input);
		alert.show();
		mShowingDialog = true;
	}

	private void setAdapter()
	{
		try
		{
			List<Secret> secrets = mDatabase.getSecrets();
			Secret[] secretsArray = new Secret[secrets.size()];
			secrets.toArray(secretsArray);
			ArrayAdapter<Secret> adapter = new ArrayAdapter<Secret>(this, android.R.layout.simple_list_item_1, secretsArray);
			setListAdapter(adapter);
			ListView lv = getListView();
			lv.removeHeaderView(mHeader);
			
		}
		catch (Exception e) 
		{
			Log.d(SafeApp.LOG_TAG, "Problem in setAdapter (SafeDefaultActivity class)");
		}
	}
	
	private void newSecret()
	{
		if (!mDatabase.ready()) return;
		
		Intent i = new Intent(this, NewSecretActivity.class);
		startActivity(i);
	}

}
