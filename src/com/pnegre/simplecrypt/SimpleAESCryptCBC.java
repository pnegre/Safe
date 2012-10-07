package com.pnegre.simplecrypt;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

/**
 * User: pnegre
 * Date: 07/10/12
 * Time: 22:23
 */
public class SimpleAESCryptCBC {
    private SimpleAESCipher2 simpleCipher;

    public SimpleAESCryptCBC(byte[] masterPw, byte[] iv) {
        Key secretKey = new Key256AES(masterPw);
        simpleCipher = new SimpleAESCipher2(secretKey, iv);
    }

    public byte[] crypt(byte[] clear) {
        simpleCipher.init(Cipher.ENCRYPT_MODE);
        return simpleCipher.doFinal(clear);
    }

    public byte[] decrypt(byte[] crypted) {
        simpleCipher.init(Cipher.DECRYPT_MODE);
        return simpleCipher.doFinal(crypted);
    }

    public OutputStream cryptedOutputStream(OutputStream out) {
        simpleCipher.init(Cipher.ENCRYPT_MODE);
        return new CipherOutputStream(out, simpleCipher.getCipher());
    }

    public InputStream decryptedInputStream(InputStream in) {
        simpleCipher.init(Cipher.DECRYPT_MODE);
        return new CipherInputStream(in, simpleCipher.getCipher());
    }
}

