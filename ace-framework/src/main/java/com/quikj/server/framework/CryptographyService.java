/**
 * 
 */
package com.quikj.server.framework;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * @author amit
 *
 */
public class CryptographyService {

	private SecretKey desKey;
	private Cipher desCipher;

	public void init() {
		try {
			KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
			desKey = keygenerator.generateKey();
			desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		} catch (Exception e) {
			throw new AceRuntimeException(e);
		}
	}

	public String encrypt(String text) {
		try {
			desCipher.init(Cipher.ENCRYPT_MODE, desKey);
			byte[] encrypted = desCipher.doFinal(text.getBytes());
			return new String(Base64.getEncoder().encode(encrypted));
		} catch (Exception e) {
			throw new AceRuntimeException(e);
		}
	}

	public String decrypt(String encrypted) {
		try {
			desCipher.init(Cipher.DECRYPT_MODE, desKey);
			byte[] decoded = Base64.getDecoder().decode(encrypted.getBytes());
			return new String(desCipher.doFinal(decoded));			
		} catch (Exception e) {
			throw new AceRuntimeException(e);
		}
	}
}
