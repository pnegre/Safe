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

/**
 * Cipher simplificat (transforma les excepcions en runtime)
 */
class SimpleAESCipher {
    private Cipher cipher;
    private Key key;

    SimpleAESCipher(Key k) {
        try {
            key = k;
            cipher = Cipher.getInstance("AES"); // AES in CBC mode (stream cipher)
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException();
        }
    }

    void init(int mode) {
        try {
            cipher.init(mode, key);
        } catch (InvalidKeyException e) {
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


class SimpleAESCipher2 {
    private Cipher cipher;
    private Key key;
    private byte[] iv;

    SimpleAESCipher2(Key k, byte[] iv) {
        try {
            key = k;
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // AES in CBC mode (stream cipher)
            this.iv = iv;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException();
        }
    }

    void init(int mode) {
        try {
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

