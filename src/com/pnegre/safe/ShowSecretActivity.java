package com.pnegre.safe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.pnegre.safe.database.Database;
import com.pnegre.safe.database.Secret;


public class ShowSecretActivity extends Activity {
    private boolean mShowPassword = false;
    private SafeApp mApp;
    private Database mDatabase;
    private Secret mSecret;
    private TextView mTVName;
    private TextView mTVUsname;
    private TextView mTVPassword;
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

        mTVName = (TextView) findViewById(R.id.secretsitename);
        mTVUsname = (TextView) findViewById(R.id.secretsiteusname);
        mTVPassword = (TextView) findViewById(R.id.secretsitepw);

        mCBShowPw = (CheckBox) findViewById(R.id.checkpw);
        mCBShowPw.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPasswordFlip();
            }
        });
    }

    public void onResume() {
        super.onResume();
        // Refresh data
        Bundle extras = getIntent().getExtras();
        int secretId = extras.getInt("secretid");
        mSecret = mDatabase.getSecret(secretId);
        mTVName.setText(mSecret.name);
        mTVUsname.setText(mSecret.username);
        mTVPassword.setText(SafeApp.PASS_HIDE_STRING);
        mCBShowPw.setChecked(false);
        mShowPassword = false;

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
        mShowPassword = !mShowPassword;

        if (mShowPassword)
            mTVPassword.setText(mSecret.password);
        else
            mTVPassword.setText(SafeApp.PASS_HIDE_STRING);
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
                Intent i = new Intent(this, EditSecretActivity.class);
                i.putExtra("secretid", mSecret.id);
                startActivity(i);
                break;
        }
        return true;
    }


}
