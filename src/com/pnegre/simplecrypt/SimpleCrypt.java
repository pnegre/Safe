package com.pnegre.simplecrypt;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:49
 */

public class SimpleCrypt {
    private SimpleAESCipher simpleCipher;

    public SimpleCrypt(byte[] masterPw) {
        Key secretKey = new Key256AES(masterPw);
        simpleCipher = new SimpleAESCipher(secretKey);
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
