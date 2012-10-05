package com.pnegre.safe;


import com.pnegre.safe.database.Secret;

public class NewSecretActivity extends CommonSecret {

    @Override
    protected void saveNewSecret(String sname, String pw, String usname) {
        Secret s = new Secret(0, sname, usname, pw);
        mDatabase.newSecret(s);
        finish();
    }

}