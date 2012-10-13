package com.pnegre.safe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.pnegre.safe.database.Database;
import com.pnegre.safe.database.EncryptedDatabase;
import com.pnegre.safe.database.SQLDatabase;

/**
 * Main application entry point
 */
public class InitialActivity extends Activity {

    private SafeApp mApp;
    private Button mBTmasterSecret;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (SafeApp) getApplication();
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
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setTransformationMethod(new android.text.method.PasswordTransformationMethod().getInstance());

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pw = input.getText().toString();
                InitDatabase(pw);

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

    private void InitDatabase(String pw) {
        try {
            Database db = new EncryptedDatabase(new SQLDatabase(mApp), mApp, pw, false);
            if (db.ready()) {
                mApp.setDatabase(db);
                invalidateOptionsMenu();
                mApp.masterPassword = pw;

                startMainActivity();
                finish();
            }
        }
        catch (EncryptedDatabase.PasswordIncorrectException e) {
            Toast.makeText(this, "Password Incorrect", Toast.LENGTH_SHORT).show();
        }
    }

}
