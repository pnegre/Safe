package com.pnegre.safe;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;


class SimpleCrypt
{
	private Key    mSecretKey;
	private Cipher mCipher;
	
	SimpleCrypt(String masterPw) throws Exception
	{
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(masterPw.getBytes());
		keyGenerator.init(128,sr);
		mSecretKey = keyGenerator.generateKey();
		mCipher = Cipher.getInstance("AES");
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
