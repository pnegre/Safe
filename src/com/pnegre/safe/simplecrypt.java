import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;


class SimpleCrypt
{
	private Key secretKey;
	
	SimpleCrypt(String masterPw) throws Exception
	{
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(masterPw.getBytes());
		keyGenerator.init(128,sr);
		secretKey = keyGenerator.generateKey();
	}
	
	String crypt(String clear) throws Exception
	{
		Cipher myCipher = Cipher.getInstance("AES");
		myCipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] result = myCipher.doFinal(clear.getBytes());
		return Base64.encodeBytes(result);
	}
	
	String decrypt(String s) throws Exception
	{
		byte[] d = Base64.decode(s.getBytes());
		
		Cipher myCipher = Cipher.getInstance("AES");
		myCipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] result = myCipher.doFinal(d);
		return new String(result);
	}
}
