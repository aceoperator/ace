/**
 * 
 */
package com.quikj.server.framework;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author amit
 *
 */
public class CryptographyService {
	private String salt;
	private Cipher cipher;

	private static CryptographyService instance;

	public String getSalt() {
		return salt;
	}

	public CryptographyService(String salt) {
		this.salt = salt;
		CryptographyService.instance = this;
	}

	public static CryptographyService getInstance() {
		return instance;
	}

	@PostConstruct
	public void init() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidParameterSpecException {
		cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
	}

	public String cipher(String plaintext, String password)
			throws InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		DESKeySpec spec = new DESKeySpec((password + salt).getBytes());
		SecretKey secretKey = SecretKeyFactory.getInstance("DES").generateSecret(spec);
		SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), "DES");

		cipher.init(Cipher.ENCRYPT_MODE, secretSpec);

		byte[] encryptedTextBytes = cipher.doFinal(plaintext.getBytes());

		return Base64.getEncoder().encodeToString(encryptedTextBytes);
	}

	public String decipher(String encryptedText, String password)
			throws InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		DESKeySpec spec = new DESKeySpec((password + salt).getBytes());
		SecretKey secretKey = SecretKeyFactory.getInstance("DES").generateSecret(spec);
		SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), "DES");

		cipher.init(Cipher.DECRYPT_MODE, secretSpec);

		byte[] encryptedTextBytes = Base64.getDecoder().decode(encryptedText);
		byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);

		return new String(decryptedTextBytes);
	}
}
