package com.pnegre.safe;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;

import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class SafeActivity extends ListActivity
{
	private Database database;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		database = new DatabaseImp();
	}
	
	public void onResume()
	{
		super.onResume();
		AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
		alert.setTitle("Password");
		// Set an EditText view to get user input   
		final EditText input = new EditText(this);
		input.setTransformationMethod(new android.text.method.PasswordTransformationMethod().getInstance());

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				InitDatabase(input.getText().toString());
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {  
			}
		});
 
		alert.setView(input);
		alert.show();
	}

	void InitDatabase(String password)
	{
		database.init(password);
		Secret[] secrets = database.getSecrets();
		ArrayAdapter<Secret> adapter = new ArrayAdapter<Secret>(this, android.R.layout.simple_list_item_1, secrets);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		Secret item = (Secret) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	}
}
