package com.pnegre.safe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


public class ShowSecretActivity extends Activity {
    private boolean mShowPassword = false;
    private SafeApp mApp;
    private Database mDatabase;
    private Secret mSecret;
    private TextView mTVName;
    private TextView mTVUsname;
    private TextView mTVPassword;
    private Button mButtonDel;
    private CheckBox mCBShowPw;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showsecret);
        mApp = (SafeApp) getApplication();
        mDatabase = mApp.getDatabase();

        Bundle extras = getIntent().getExtras();
        int secretId = extras.getInt("secretid");
        mSecret = mDatabase.getSecret(secretId);
        mTVName = (TextView) findViewById(R.id.secretsitename);
        mTVName.setText(mSecret.name);
        mTVUsname = (TextView) findViewById(R.id.secretsiteusname);
        mTVUsname.setText(mSecret.username);
        mTVPassword = (TextView) findViewById(R.id.secretsitepw);
        showPasswordFlip();

        mCBShowPw = (CheckBox) findViewById(R.id.checkpw);
        mCBShowPw.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPasswordFlip();
            }
        });
    }

    // Mostra diàleg per esborrar el secret
    void showDeleteDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are you sure?");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteSecret();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    // Esborra el secret
    void deleteSecret() {
        mDatabase.deleteSecret(mSecret.id);
        finish();
    }

    // Mostra "XXXXXXXX" o la password, cada vegada que es crida la funció
    // O sigui, va alternant.
    void showPasswordFlip() {
        if (mShowPassword == false)
            mTVPassword.setText(SafeApp.PASS_HIDE_STRING);
        else
            mTVPassword.setText(mSecret.password);

        mShowPassword = !mShowPassword;
    }

    // Inflate res/menu/menuactivity.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.removesecret:
                showDeleteDialog();
                break;

            case R.id.editsecret:
                showChangePWDialog();
                break;
        }
        return true;
    }


    private void showChangePWDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New Password");
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setSingleLine();
        input.setTransformationMethod(new android.text.method.PasswordTransformationMethod().getInstance());
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mSecret.password = input.getText().toString();
                mDatabase.updateSecret(mSecret);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.setView(input);
        alert.show();
    }



}
