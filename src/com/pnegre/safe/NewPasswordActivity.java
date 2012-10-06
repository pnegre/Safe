package com.pnegre.safe;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
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
    private SeekBar seekBar;
    private TextView pwlen;

    private RandPass rpass = new RandPass();
    private int passLenght = 8;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newpassword);

        butGenerate = (Button) findViewById(R.id.pwgenerate);
        generatedPw = (TextView) findViewById(R.id.pwgenerated);
        cbMixed = (CheckBox) findViewById(R.id.pwmixed);
        cbSymbols = (CheckBox) findViewById(R.id.pwsymbols);
        cbNumbers = (CheckBox) findViewById(R.id.pwnumbers);
        seekBar = (SeekBar) findViewById(R.id.pwseekbar);
        seekBar.setMax(11);
        seekBar.setProgress(3);
        pwlen = (TextView) findViewById(R.id.pwlen);

        butGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePassword();

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                passLenght = 5 + i;
                pwlen.setText("Password Length: " + Integer.toString(passLenght));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
        generatedPw.setText(rpass.getPass(passLenght));

    }
}
