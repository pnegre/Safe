package com.pnegre.safe;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.CheckBox;


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
			final int secretId = extras.getInt("secretid");
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
					try
					{
						database.deleteSecret(secretId);
					} catch (Exception e) { }
					finish();
				}
			});
		} 
		catch (Exception e) { }
	}
	
	// Mostra "XXXXXXXX" o la password, cada vegada que es crida la funció
	// O sigui, va alternant.
	void showPasswordFlip()
	{
		if (showPassword == false)
			tpw.setText("XXXXXXXX");
		else
			tpw.setText(theSecret.password);
		
		showPassword = ! showPassword;
	}
}
