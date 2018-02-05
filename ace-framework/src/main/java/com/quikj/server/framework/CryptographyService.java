/**
 * 
 */
package com.quikj.server.framework;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author amit
 *
 */
public class CryptographyService {
	private String salt = "SALT1";

	private static final int ITERATIONS = 65536;
	private static final int KEYSIZE = 256;
	private byte[] saltBytes;
	private Cipher cipher;
	private SecretKeyFactory skf;
	private IvParameterSpec ivParamSpec;

	public CryptographyService(String salt) {
		this.salt = salt;
	}

	public void init() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidParameterSpecException {
			saltBytes = salt.getBytes();
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			AlgorithmParameters params = cipher.getParameters();
			byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
			ivParamSpec = new IvParameterSpec(ivBytes);
	}

	public String cipher(String plaintext, String password)
			throws InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, InvalidAlgorithmParameterException {
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEYSIZE);
		SecretKey secretKey = skf.generateSecret(spec);
		SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

		cipher.init(Cipher.ENCRYPT_MODE, secretSpec, ivParamSpec);

		byte[] encryptedTextBytes = cipher.doFinal(plaintext.getBytes());

		return Base64.getEncoder().encodeToString(encryptedTextBytes);
	}

	public String decipher(String encryptedText, String password)
			throws InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, InvalidAlgorithmParameterException {
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEYSIZE);
		SecretKey secretKey = skf.generateSecret(spec);
		SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

		cipher.init(Cipher.DECRYPT_MODE, secretSpec, ivParamSpec);

		byte[] encryptedTextBytes = Base64.getDecoder().decode(encryptedText);
		byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);

		return new String(decryptedTextBytes);
	}
}
