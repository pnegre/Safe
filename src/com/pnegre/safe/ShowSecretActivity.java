package com.pnegre.safe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

        try {
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

            mButtonDel = (Button) findViewById(R.id.butdelsecret);
            mButtonDel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showDeleteDialog();
                }
            });
        } catch (Exception e) {
            Log.d(SafeApp.LOG_TAG, "Exception in ShowSecretActivity.onCreate");
        }
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
        try {
            mDatabase.deleteSecret(mSecret.id);
        } catch (Exception e) {
            Log.d(SafeApp.LOG_TAG, "Exception deleting secret");
        }
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
}
