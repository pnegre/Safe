package com.pnegre.safe;

import java.util.Random;

/**
 * User: pnegre
 * Date: 06/10/12
 * Time: 11:28
 */


class RandPass {
    static final int UPPER = 0x1;
    static final int NUMBERS = 0x2;
    static final int SYMBOLS = 0x4;

    static private String alph_lower = "abcdefghijklmnopqrstuvwxyz";
    static private String alph_numbers = "0123456789";
    static private String alph_symbols = "|@#<>!$%&/()=?¿{[]}";

    private String theAlph = alph_lower;
    private Random rnd = new Random();

    RandPass(int alph) {
        setAlphabet(alph);
    }

    RandPass() {
    }

    void setAlphabet(int alph) {
        theAlph = alph_lower;
        if ((alph & UPPER) != 0)
            theAlph += theAlph.toUpperCase();
        if ((alph & NUMBERS) != 0)
            theAlph += alph_numbers;
        if ((alph & SYMBOLS) != 0)
            theAlph += alph_symbols;

    }

    String getPass(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i=0; i<len; i++) {
            sb.append(theAlph.charAt(rnd.nextInt(theAlph.length())));
        }
        return sb.toString();
    }
}