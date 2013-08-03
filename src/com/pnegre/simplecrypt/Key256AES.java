package com.pnegre.simplecrypt;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:49
 */

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
        realKey = new byte[32];
        byte[] digest = calculateDigest(initialKey);
        int i = 0, j = 0;
        while (i < 32) {
            realKey[i++] = digest[j];
            j = (j + 1) % digest.length;
        }
    }

    private byte[] calculateDigest(byte[] k) {
        final int NROUNDS = 1;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(k);
            for (int i = 1; i < NROUNDS; i++) {
                byte[] dd = md.digest();
                md.reset();
                md.update(dd);
                md.update(k);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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