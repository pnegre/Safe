package com.pnegre.safe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import com.pnegre.safe.database.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;


abstract class CommonSecret extends Activity {
    protected EditText mETsitename, mETusername, mETpassword;
    protected SafeApp mApp;
    protected Database mDatabase;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsecret);

        mETsitename = (EditText) findViewById(R.id.sitename);
        mETusername = (EditText) findViewById(R.id.siteusname);
        mETpassword = (EditText) findViewById(R.id.sitepassword);
    }

    public void onResume() {
        super.onResume();
        mApp = (SafeApp) getApplication();
        mDatabase = mApp.getDatabase();

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

            case R.id.genpassword:
                mETpassword.setText(generatePassword(8));
                mETpassword.requestFocus();
                break;
        }

        return true;
    }

    protected void newSecret() {
        String sname = mETsitename.getText().toString();
        String usname = mETusername.getText().toString();
        String pw = mETpassword.getText().toString();
        if (sname.equals("") || pw.equals("") || usname.equals("")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("No Blanks!");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        } else {
            saveNewSecret(sname, pw, usname);
        }
    }

    protected String generatePassword(int len) {


        RandPass rp = new RandPass(RandPass.NUMBERS | RandPass.UPPER);
        return rp.getPass(8);
    }

    protected abstract void saveNewSecret(String sname, String pw, String usname);
}




