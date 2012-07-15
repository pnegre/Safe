package com.pnegre.safe;

import javax.crypto.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;


// Builds a 256bit AES-compatible key from a string or a bunch of raw bytes
class Key256AES implements Key, KeySpec, SecretKey {
    private byte[] realKey;

    // Constructors
    Key256AES(byte[] rawBytes) {
        deriveRealKey(rawBytes);
    }

    Key256AES(String _string) {
        deriveRealKey(_string.getBytes());
    }

    private void deriveRealKey(byte[] initialKey) {
        try {
            realKey = new byte[32];
            MessageDigest md = null;
            md = MessageDigest.getInstance("SHA-512");
            md.update(initialKey);
            byte[] digest = md.digest();
            int i = 0, j = 0;
            while (i < 32) {
                realKey[i++] = digest[j];
                j = (j + 1) % digest.length;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm SHA-512 not supported");
        }
    }

    public String getAlgorithm() {
        return "AES";
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        return realKey;
    }
}


class SimpleCrypt {
    private SimpleAESCipher simpleCipher;

    SimpleCrypt(byte[] masterPw) {
        Key secretKey = new Key256AES(masterPw);
        simpleCipher = new SimpleAESCipher(secretKey);
    }

    byte[] crypt(byte[] clear) {
        simpleCipher.init(Cipher.ENCRYPT_MODE);
        return simpleCipher.doFinal(clear);
    }

    byte[] decrypt(byte[] crypted) {
        simpleCipher.init(Cipher.DECRYPT_MODE);
        return simpleCipher.doFinal(crypted);
    }

    OutputStream cryptedOutputStream(OutputStream out) {
        simpleCipher.init(Cipher.ENCRYPT_MODE);
        return new CipherOutputStream(out, simpleCipher.getCipher());
    }

    InputStream decryptedInputStream(InputStream in) {
        simpleCipher.init(Cipher.DECRYPT_MODE);
        return new CipherInputStream(in, simpleCipher.getCipher());
    }
}

/**
 * Cipher simplificat (transforma les excepcions en runtime)
 */
class SimpleAESCipher {
    private Cipher cipher;
    private Key key;

    SimpleAESCipher(Key k) {
        try {
            key = k;
            cipher = Cipher.getInstance("AES");
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

