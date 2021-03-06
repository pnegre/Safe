package com.pnegre.safe;


import android.os.Bundle;
import android.view.View;
import com.pnegre.safe.database.Secret;

public class EditSecretActivity extends CommonSecret {

    private Secret mSecret;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mETpassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                String t = mETpassword.getText().toString();

                if (hasFocus) {
                    if (t.equals(SafeApp.PASS_HIDE_STRING)) {
                        mETpassword.setText("");
                    }
                } else {
                    if (t.equals("")) {
                        mETpassword.setText(SafeApp.PASS_HIDE_STRING);
                    }
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDatabase == null) {
            SafeApp.initMainActivity(this);
            finish();
            return;
        }

        Bundle extras = getIntent().getExtras();
        int secretId = extras.getInt("secretid");
        mSecret = mDatabase.getSecret(secretId);
        mETsitename.setText(mSecret.name);
        mETusername.setText(mSecret.username);
        mETpassword.setText(SafeApp.PASS_HIDE_STRING);

    }

    @Override
    protected void saveNewSecret(String sname, String pw, String usname) {
        mSecret.name = sname;
        mSecret.username = usname;
        // update password only if user has provided a new one
        if (!pw.equals(SafeApp.PASS_HIDE_STRING)) mSecret.password = pw;
        mDatabase.updateSecret(mSecret);
        finish();
    }

}