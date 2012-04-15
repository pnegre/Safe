package com.pnegre.safe;

import javax.crypto.*;
import java.io.ByteArrayInputStream;
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

    byte[] crypt(byte[] clear) throws Exception {
        initCipher(Cipher.ENCRYPT_MODE);
        byte[] result = theCipher.doFinal(clear);
        return result;
    }

    byte[] decrypt(byte[] crypted) throws Exception {
        initCipher(Cipher.DECRYPT_MODE);
        byte[] result = theCipher.doFinal(crypted);
        return result;
    }

    OutputStream cryptedOutputStream(OutputStream out) throws Exception {
        initCipher(Cipher.ENCRYPT_MODE);
        return new CipherOutputStream(out,theCipher);
    }

    InputStream decryptedInputStream(InputStream in) throws  Exception {
        initCipher(Cipher.DECRYPT_MODE);
        return new CipherInputStream(in, theCipher);
    }

    public void cryptFile(InputStream in, OutputStream out) throws Exception {
        // Bytes written to out will be encrypted
        initCipher(Cipher.ENCRYPT_MODE);
        out = new CipherOutputStream(out, theCipher);

        process(in, out);
        out.close();
    }

    public void decryptFile(InputStream in, OutputStream out) throws Exception {
        // Bytes read from in will be decrypted
        initCipher(Cipher.DECRYPT_MODE);
        in = new CipherInputStream(in, theCipher);

        process(in, out);
        out.close();
    }

    public void cryptRawData(byte[] data, OutputStream out) throws Exception {
        initCipher(Cipher.ENCRYPT_MODE);
        InputStream in = new ByteArrayInputStream(data);
        out = new CipherOutputStream(out, theCipher);

        process(in, out);
        out.close();
    }

    private void process(InputStream in, OutputStream out) throws Exception {
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    }
}

