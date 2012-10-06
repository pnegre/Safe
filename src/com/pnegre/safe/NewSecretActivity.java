package com.pnegre.safe;


import android.os.Bundle;
import android.widget.EditText;
import com.pnegre.safe.database.Secret;

public class NewSecretActivity extends CommonSecret {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String pw = extras.getString("password");
            if (pw != null)
                mETpassword.setText(pw);
        }
    }

    @Override
    protected void saveNewSecret(String sname, String pw, String usname) {
        Secret s = new Secret(0, sname, usname, pw);
        mDatabase.newSecret(s);
        finish();
    }

}