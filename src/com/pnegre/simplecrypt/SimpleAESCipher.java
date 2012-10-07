package com.pnegre.simplecrypt;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:49
 */

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Cipher simplificat (transforma les excepcions en runtime)
 */
class SimpleAESCipher {
    private Cipher cipher;
    private Key key;

    SimpleAESCipher(Key k) {
        try {
            key = k;
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // AES in CBC mode (stream cipher)
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException();
        }
    }

    void init(int mode) {
        try {
            byte[] iv = new byte[16];

            iv[0] = 10;
            iv[1] = 12;
            iv[2] = 40;
            iv[3] = 17;
            iv[4] = 17;
            iv[5] = 120;
            iv[6] = 120;
            iv[7] = 110;
            iv[8] = -3;
            iv[9] = 5;
            iv[10] = 120;
            iv[11] = 120;
            iv[12] = 110;
            iv[13] = -3;
            iv[14] = 5;
            iv[15] = 120;

            cipher.init(mode, key, new IvParameterSpec(iv));
        } catch (InvalidKeyException e) {
            throw new RuntimeException();
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException();
        }
    }

    byte[] doFinal(byte[] in) {
        try {
            return cipher.doFinal(in);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException();
        } catch (BadPaddingException e) {
            throw new RuntimeException();
        }
    }

    Cipher getCipher() {
        return cipher;
    }
}


