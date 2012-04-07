package com.pnegre.safe;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.KeySpec;


// Builds a 256bit AES-compatible key from a string or a bunch of raw bytes
class Key256AES implements Key, KeySpec, SecretKey {
    private byte[] realKey;

    // Constructors
    Key256AES(byte[] rawBytes) throws Exception {
        deriveRealKey(rawBytes);
    }

    Key256AES(String _string) throws Exception {
        deriveRealKey(_string.getBytes());
    }

    private void deriveRealKey(byte[] initialKey) throws Exception {
        realKey = new byte[32];
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(initialKey);
        byte[] digest = md.digest();
        int i = 0, j = 0;
        while (i < 32) {
            realKey[i++] = digest[j];
            j = (j + 1) % digest.length;
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

    SimpleCrypt(byte[] masterPw) throws Exception {
        secretKey = new Key256AES(masterPw);
        theCipher = Cipher.getInstance("AES");
    }

    byte[] crypt(byte[] clear) throws Exception {
        theCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] result = theCipher.doFinal(clear);
        return result;
    }

    byte[] decrypt(byte[] crypted) throws Exception {
        theCipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] result = theCipher.doFinal(crypted);
        return result;
    }

    public void cryptFile(InputStream in, OutputStream out) throws Exception {
        // Bytes written to out will be encrypted
        theCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        out = new CipherOutputStream(out, theCipher);

        process(in, out);
        out.close();
    }

    public void decryptFile(InputStream in, OutputStream out) throws Exception {
        // Bytes read from in will be decrypted
        theCipher.init(Cipher.DECRYPT_MODE, secretKey);
        in = new CipherInputStream(in, theCipher);

        process(in, out);
        out.close();
    }

    public void cryptRawData(byte[] data, OutputStream out) throws Exception {
        theCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
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

