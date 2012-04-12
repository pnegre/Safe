package com.pnegre.safe;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;


public class SafeDefaultActivity extends ListActivity {
    private SafeApp mApp;
    private Database mDatabase;
    private boolean mShowingDialog = false;
    private ViewGroup mHeader;
    private Button mBTmasterSecret;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (SafeApp) getApplication();
        mDatabase = mApp.getDatabase();
        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        mHeader = (ViewGroup) inflater.inflate(R.layout.header, lv, false);
        lv.addHeaderView(mHeader, null, false);
        setListAdapter(null);

        mBTmasterSecret = (Button) findViewById(R.id.butmastersecret);
        mBTmasterSecret.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showMasterPwDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApp.setMenuVisibility(false);
        if (mDatabase != null)
            mDatabase.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShowingDialog) return;
        if (mDatabase == null) return;

        if (mDatabase.ready() == true)
            setAdapter();
    }

    // Inflate res/menu/mainmenu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);

        if (mApp.menuVisible()) {
            MenuItem mi = menu.findItem(R.id.newsecret);
            mi.setVisible(true);
        }

        return true;
    }


    // Respond to user click on menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changepw:
                if (!mDatabase.ready())
                    // TODO: aquí codi per canviar la master password
                    return true;



                return true;

            case R.id.newsecret:
                newSecret();
                return true;

            case R.id.exportsecrets:
                exportSecrets();
                return true;

            case R.id.importsecrets:
                importSecrets();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void importSecrets() {
        try {
            final Backup backup = new Backup(mDatabase);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Choose");
            final CharSequence[] items = backup.enumerateFiles();
            alert.setItems(items, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialogInterface, int item) {
                    try {
                        String chosen = (String) items[item];
                        backup.doImport(chosen, mApp.masterPassword);
                    } catch (Exception e) {}
                    setAdapter();
                }
            });
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Exportar secrets mitjançant un XML (segurament també encriptat)
    private void exportSecrets() {
        try {
            Backup backup = new Backup(mDatabase);
            backup.doExport(mApp.masterPassword);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Secret item = (Secret) getListAdapter().getItem(position);
        int secretId = item.id;
        Intent i = new Intent(this, ShowSecretActivity.class);
        i.putExtra("secretid", secretId);
        startActivity(i);
    }


    private void showSelectBackupDialog() {

    }


    private void showMasterPwDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Password");
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setTransformationMethod(new android.text.method.PasswordTransformationMethod().getInstance());

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pw = input.getText().toString();
                Database db = new EncryptedDatabase(new SQLDatabase(SafeDefaultActivity.this), SafeDefaultActivity.this, pw);
                if (db.ready()) {
                    mDatabase = db;
                    mApp.setDatabase(db);
                    setAdapter();
                    mShowingDialog = false;

                    mApp.setMenuVisibility(true);
                    invalidateOptionsMenu();
                    mApp.masterPassword = pw;
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mShowingDialog = false;
            }
        });

        alert.setView(input);
        alert.show();
        mShowingDialog = true;
    }

    private void setAdapter() {
        try {
            if (!mDatabase.ready()) return;
            List<Secret> secrets = mDatabase.getSecrets();
            Secret[] secretsArray = new Secret[secrets.size()];
            secrets.toArray(secretsArray);
            ArrayAdapter<Secret> adapter = new ArrayAdapter<Secret>(this, android.R.layout.simple_list_item_1, secretsArray);
            setListAdapter(adapter);
            ListView lv = getListView();
            lv.removeHeaderView(mHeader);

        } catch (Exception e) {
            Log.d(SafeApp.LOG_TAG, "Problem in setAdapter (SafeDefaultActivity class)");
            e.printStackTrace();
        }
    }

    private void newSecret() {
        if (!mDatabase.ready()) return;

        Intent i = new Intent(this, NewSecretActivity.class);
        startActivity(i);
    }

}
