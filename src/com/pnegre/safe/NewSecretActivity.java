package com.pnegre.safe;

import android.os.Bundle;
import android.app.Activity;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;

public class NewSecretActivity extends Activity
{
	private EditText sitenameET, siteusnameET, sitepasswordET;
	private Button newsecretBT;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newsecret);
		
		sitenameET = (EditText) findViewById(R.id.sitename);
		siteusnameET = (EditText) findViewById(R.id.siteusname);
		sitepasswordET = (EditText) findViewById(R.id.sitepassword);
		
		newsecretBT = (Button) findViewById(R.id.butnewsecret);
		newsecretBT.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				newSecret();
			}
		});
	}
	
	void newSecret()
	{
	}
}
