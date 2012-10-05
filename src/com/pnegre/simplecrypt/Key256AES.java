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