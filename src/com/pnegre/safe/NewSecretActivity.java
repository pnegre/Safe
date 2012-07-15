package com.pnegre.safe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewSecretActivity extends Activity {
    private EditText mETsitename, mETusername, mETpassword;
    private SafeApp mApp;
    Database mDatabase;
    Secret mSecret = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsecret);
        mApp = (SafeApp) getApplication();
        mDatabase = mApp.getDatabase();

        mETsitename = (EditText) findViewById(R.id.sitename);
        mETusername = (EditText) findViewById(R.id.siteusname);
        mETpassword = (EditText) findViewById(R.id.sitepassword);

        // See if callee has included the "secretid" parameter
        try {
            Bundle extras = getIntent().getExtras();
            int secretId = extras.getInt("secretid");
            mSecret = mDatabase.getSecret(secretId);
            mETsitename.setText(mSecret.name);
            mETusername.setText(mSecret.username);
        }
        catch (java.lang.NullPointerException npe) { }



    }

    void newSecret() {
        String sname = mETsitename.getText().toString();
        String usname = mETusername.getText().toString();
        String pw = mETpassword.getText().toString();
        if (sname.equals("") || pw.equals("")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("No Blanks!");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        } else {
            // Create new secret or update an existing one
            if (mSecret == null) {
                Secret s = new Secret(0, sname, usname, pw);
                mDatabase.newSecret(s);
            } else {
                mSecret.name = sname;
                mSecret.username = usname;
                // update password only if user has provided a new one
                if (pw.length() > 0) mSecret.password = pw;
                mDatabase.updateSecret(mSecret);
            }
            finish();
        }
    }


    // Inflate res/menu/menuactivity.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newsecretmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secretsave:
                newSecret();
                break;
        }

        return true;
    }
}
