package com.pnegre.safe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Main application entry point
 *
 */
public class InitialActivity extends Activity {

    private SafeApp mApp;
    private Button  mBTmasterSecret;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (SafeApp) getApplication();
        mApp.context = this;
        setContentView(R.layout.initialscreen);

        mBTmasterSecret = (Button) findViewById(R.id.butmastersecret);
        mBTmasterSecret.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showMasterPwDialog();
            }
        });
    }



    private void showMasterPwDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Password");
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setSingleLine();
        input.setTransformationMethod(new android.text.method.PasswordTransformationMethod().getInstance());
//        input.setTransformationMethod(new android.text.method.SingleLineTransformationMethod().getInstance());

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pw = input.getText().toString();
                Database db = new EncryptedDatabase(new SQLDatabase(mApp.context), mApp.context, pw, false);
                if (db.ready()) {
                    mApp.setDatabase(db);
                    mApp.setMenuVisibility(true);
                    invalidateOptionsMenu();
                    mApp.masterPassword = pw;
                    InitialActivity.this.finish();
                    startMainActivity();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setView(input);
        alert.show();
    }



    private void startMainActivity() {
        Intent i = new Intent(this, SafeMainActivity.class);
        startActivity(i);
    }

}
