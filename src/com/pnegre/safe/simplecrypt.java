package com.pnegre.safe;

import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;


class SimpleCrypt
{
	private SecretKeySpec mSecretKey;
	Cipher mCipher;
	byte[] buf = new byte[1024];
	
	SimpleCrypt(String masterPw) throws Exception
	{
		byte[] key = get256Key(masterPw.getBytes());
		mSecretKey = new SecretKeySpec(key,"AES");
		mCipher = Cipher.getInstance("AES");
	}
	
	
	// Takes an initial byte array and returns a 256bit key (32 bytes)
	// Applies a MD5 digest to the initialKey provided by the user
	private byte[] get256Key(byte[] initialKey) throws java.security.NoSuchAlgorithmException
	{
		byte[] result = new byte[32];
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(initialKey);
		byte[] digest = md.digest();
		int i=0,j=0;
		while (i<32)
		{
			result[i++] = digest[j];
			j = (j + 1) % digest.length;
		}
		return result;
	}

	String crypt(String clear) throws Exception
	{
		mCipher.init(Cipher.ENCRYPT_MODE, mSecretKey);
		byte[] result = mCipher.doFinal(clear.getBytes());
		return Base64.encodeBytes(result);
	}
	
	String decrypt(String encrypted) throws Exception
	{
		byte[] d = Base64.decode(encrypted.getBytes());
		mCipher.init(Cipher.DECRYPT_MODE, mSecretKey);
		byte[] result = mCipher.doFinal(d);
		return new String(result);
	}
}
