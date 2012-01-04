package com.pnegre.safe;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;

public class SafeActivity extends ListActivity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Database db = new DatabaseImp();
		Secret[] secrets = db.getSecrets();
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
