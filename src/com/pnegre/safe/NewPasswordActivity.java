package com.pnegre.safe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

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
    private CheckBox cbPronunceable;
    private SeekBar seekBar;
    private TextView pwlen;

    static private RandPass rpass = new RandPass();
    private int passLenght = 8;
    private String thePassword = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newpassword);

        butGenerate = (Button) findViewById(R.id.pwgenerate);
        generatedPw = (TextView) findViewById(R.id.pwgenerated);
        cbMixed = (CheckBox) findViewById(R.id.pwmixed);
        cbSymbols = (CheckBox) findViewById(R.id.pwsymbols);
        cbNumbers = (CheckBox) findViewById(R.id.pwnumbers);
        cbPronunceable = (CheckBox) findViewById(R.id.pwpronunceable);
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

        cbPronunceable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    cbSymbols.setChecked(false);
                    cbSymbols.setEnabled(false);

                } else {
                    cbSymbols.setEnabled(true);
                }

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
        if (cbPronunceable.isChecked())
            thePassword = rpass.getPronunceable(passLenght);
        else
            thePassword = rpass.getPass(passLenght);

        generatedPw.setText(thePassword);

    }

    // Inflate res/menu/mainmenu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menunewpassword, menu);

        return true;
    }

    // Respond to user click on menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.savepass:
                Intent i = new Intent(this, NewSecretActivity.class);
                i.putExtra("password", thePassword);
                startActivity(i);
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
