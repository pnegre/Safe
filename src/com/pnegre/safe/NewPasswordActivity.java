package com.pnegre.safe;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Random;

/**
 * User: pnegre
 * Date: 06/10/12
 * Time: 03:05
 */
public class NewPasswordActivity extends Activity {

    private Button butGenerate;
    private TextView generatedPw;
    private CheckBox cbMixed;
    private CheckBox cbSymbols;
    private CheckBox cbNumbers;

    private RandPass rpass = new RandPass();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newpassword);

        butGenerate = (Button) findViewById(R.id.pwgenerate);
        generatedPw = (TextView) findViewById(R.id.pwgenerated);
        cbMixed = (CheckBox) findViewById(R.id.pwmixed);
        cbSymbols = (CheckBox) findViewById(R.id.pwsymbols);
        cbNumbers = (CheckBox) findViewById(R.id.pwnumbers);

        butGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePassword();

            }
        });

        generatePassword();

    }

    private void generatePassword() {
        int pars = 0;
        if (cbMixed.isChecked())
            pars += RandPass.UPPER;
        if (cbSymbols.isChecked())
            pars += RandPass.SYMBOLS;
        if (cbNumbers.isChecked())
            pars += RandPass.NUMBERS;

        rpass.setAlphabet(pars);
        generatedPw.setText(rpass.getPass(10));

    }
}
