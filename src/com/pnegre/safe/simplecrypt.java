package com.pnegre.safe;

import javax.crypto.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
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
    private Key secretKey;
    private Cipher theCipher;
    private byte[] buf = new byte[1024];

    SimpleCrypt(byte[] masterPw) {
        secretKey = new Key256AES(masterPw);
        try {
            theCipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException();
        }
    }

    private void initCipher(int mode) {
        try {
            theCipher.init(mode, secretKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Should not happen");
        }
    }

    private byte[] doFinal(byte[] in) {
        try {
            return theCipher.doFinal(in);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException();
        } catch (BadPaddingException e) {
            throw new RuntimeException();
        }
    }

    byte[] crypt(byte[] clear) {
        initCipher(Cipher.ENCRYPT_MODE);
        return doFinal(clear);
    }

    byte[] decrypt(byte[] crypted)  {
        initCipher(Cipher.DECRYPT_MODE);
        return doFinal(crypted);
    }

    OutputStream cryptedOutputStream(OutputStream out) {
        initCipher(Cipher.ENCRYPT_MODE);
        return new CipherOutputStream(out,theCipher);
    }

    InputStream decryptedInputStream(InputStream in) {
        initCipher(Cipher.DECRYPT_MODE);
        return new CipherInputStream(in, theCipher);
    }
}

