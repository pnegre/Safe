package com.pnegre.safe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.List;


public class SafeDefaultActivity extends ListActivity {
    private SafeApp mApp;
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
        Database db = mApp.getDatabase();
        if (db != null)
            db.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShowingDialog) return;

        Database db = mApp.getDatabase();
        if (db == null) return;

        if (db.ready() == true)
            setAdapter(db);
    }

    // Inflate res/menu/mainmenu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);

        if (mApp.menuVisible()) {
            menu.findItem(R.id.newsecret).setVisible(true);
            menu.findItem(R.id.importsecrets).setVisible(true);
            menu.findItem(R.id.exportsecrets).setVisible(true);
            menu.findItem(R.id.changepw).setVisible(true);
        }

        return true;
    }


    // Respond to user click on menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changepw:
                changePassword();
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

    private void changePassword() {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Change Password");
        dialog.setContentView(R.layout.dialogchangepassword);
        dialog.show();

        Button dialogButton = (Button) dialog.findViewById(R.id.butokchpass);
        final EditText et1 = (EditText) dialog.findViewById(R.id.pw1);
        final EditText et2 = (EditText) dialog.findViewById(R.id.pw2);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pw1 = et1.getText().toString();
                String pw2 = et2.getText().toString();
                if (!pw1.equals(pw2))
                    showToast("Passwords don't match");
                else {
                    dialog.dismiss();
                }
            }
        });
    }

    private void importSecrets() {
        try {
            final Backup backup = new Backup(mApp.getDatabase());
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Choose");
            final CharSequence[] items = backup.enumerateFiles();
            if (items.length == 0) {
                showToast("No backups detected");
                return;
            }

            alert.setItems(items, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialogInterface, int item) {
                    try {
                        String chosen = (String) items[item];
                        backup.doImport(chosen, mApp.masterPassword);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setAdapter(mApp.getDatabase());
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
            Backup backup = new Backup(mApp.getDatabase());
            String filename = backup.doExport(mApp.masterPassword);
            showToast(filename + " saved.");

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Can't save file");
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                    mApp.setDatabase(db);
                    setAdapter(db);
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

    private void setAdapter(Database db) {
        try {
            if (!db.ready()) return;
            List<Secret> secrets = db.getSecrets();
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
        Intent i = new Intent(this, NewSecretActivity.class);
        startActivity(i);
    }

}
