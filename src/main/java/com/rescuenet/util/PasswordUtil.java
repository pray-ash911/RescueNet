package com.rescuenet.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Prayash Rawal
 */

/**
 * Utility class for password encryption and decryption in the RescueNet
 * application. Uses AES-GCM encryption with password-derived keys for secure
 * password handling.
 */
public class PasswordUtil {

	private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
	private static final int TAG_LENGTH_BIT = 128;
	private static final int IV_LENGTH_BYTE = 12;
	private static final int SALT_LENGTH_BYTE = 16;
	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	/**
	 * Generates a random nonce of the specified length.
	 *
	 * @param numBytes the number of bytes for the nonce
	 * @return a byte array containing the random nonce
	 */
	public static byte[] getRandomNonce(int numBytes) {
		// --- Generate Nonce ---
		byte[] nonce = new byte[numBytes];
		new SecureRandom().nextBytes(nonce);
		return nonce;
	}

	/**
	 * Generates an AES secret key of the specified size.
	 *
	 * @param keysize the size of the key in bits (e.g., 256)
	 * @return the generated SecretKey
	 * @throws NoSuchAlgorithmException if the AES algorithm is not available
	 */
	public static SecretKey getAESKey(int keysize) throws NoSuchAlgorithmException {
		// --- Generate AES Key ---
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(keysize, SecureRandom.getInstanceStrong());
		return keyGen.generateKey();
	}

	/**
	 * Derives a 256-bit AES secret key from a password and salt using PBKDF2.
	 *
	 * @param password the password to derive the key from
	 * @param salt     the salt for key derivation
	 * @return the derived SecretKey, or null if an error occurs
	 * @throws NoSuchAlgorithmException if PBKDF2WithHmacSHA256 is not available
	 * @throws InvalidKeySpecException  if the key specification is invalid
	 */
	public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// --- Derive AES Key ---
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
			SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
			return secret;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			Logger.getLogger(PasswordUtil.class.getName()).log(Level.SEVERE, null, ex);
			throw ex;
		}
	}

	/**
	 * Encrypts a password using AES-GCM with a key derived from an employee ID.
	 *
	 * @param employee_id the employee ID used to derive the encryption key
	 * @param password    the password to encrypt
	 * @return a Base64-encoded string containing the encrypted password, IV, and
	 *         salt, or null if an error occurs
	 */
	public static String encrypt(String employee_id, String password) {
		try {
			// --- Generate Salt and IV ---
			byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
			byte[] iv = getRandomNonce(IV_LENGTH_BYTE);

			// --- Derive Encryption Key ---
			SecretKey aesKeyFromPassword = getAESKeyFromPassword(employee_id.toCharArray(), salt);

			// --- Encrypt Password ---
			Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
			byte[] cipherText = cipher.doFinal(password.getBytes());

			// --- Combine IV, Salt, and Cipher Text ---
			byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length).put(iv)
					.put(salt).put(cipherText).array();

			// --- Encode to Base64 ---
			return Base64.getEncoder().encodeToString(cipherTextWithIvSalt);
		} catch (Exception ex) {
			Logger.getLogger(PasswordUtil.class.getName()).log(Level.SEVERE, "Encryption failed", ex);
			return null;
		}
	}

	/**
	 * Decrypts a Base64-encoded encrypted password using AES-GCM with a key derived
	 * from a username.
	 *
	 * @param encryptedPassword the Base64-encoded encrypted password
	 * @param username          the username used to derive the decryption key
	 * @return the decrypted password, or null if an error occurs
	 */
	public static String decrypt(String encryptedPassword, String username) {
		try {
			// --- Decode Base64 Input ---
			byte[] decode = Base64.getDecoder().decode(encryptedPassword.getBytes(UTF_8));

			// --- Extract IV, Salt, and Cipher Text ---
			ByteBuffer bb = ByteBuffer.wrap(decode);
			byte[] iv = new byte[IV_LENGTH_BYTE];
			bb.get(iv);
			byte[] salt = new byte[SALT_LENGTH_BYTE];
			bb.get(salt);
			byte[] cipherText = new byte[bb.remaining()];
			bb.get(cipherText);

			// --- Derive Decryption Key ---
			SecretKey aesKeyFromPassword = PasswordUtil.getAESKeyFromPassword(username.toCharArray(), salt);

			// --- Decrypt Password ---
			Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
			byte[] plainText = cipher.doFinal(cipherText);

			// --- Convert to String ---
			return new String(plainText, UTF_8);
		} catch (Exception ex) {
			Logger.getLogger(PasswordUtil.class.getName()).log(Level.SEVERE, "Decryption failed", ex);
			return null;
		}
	}
}