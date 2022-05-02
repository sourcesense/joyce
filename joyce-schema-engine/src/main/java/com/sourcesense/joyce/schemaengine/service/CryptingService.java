package com.sourcesense.joyce.schemaengine.service;

import com.sourcesense.joyce.schemaengine.exception.CryptingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CryptingService {

	private final SecretKey secretKey;

	private final static Integer IV_LENGTH = 16;
	private final static String CYPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

	public String decrypt(String crypted) {
		try {
			byte[] base64Decoded = Base64.getDecoder().decode(crypted);
			IvParameterSpec iv = new IvParameterSpec(base64Decoded, 0, IV_LENGTH);
			byte[] cipherText = Arrays.copyOfRange(base64Decoded, IV_LENGTH, base64Decoded.length);
			Cipher decryptCipher = this.computeCipher(secretKey, iv);
			return new String(decryptCipher.doFinal(cipherText));

		} catch (Exception exception) {
			throw new CryptingException(
					String.format("Decrypt failed, error message is: '%s'", exception.getMessage())
			);
		}
	}

	private Cipher computeCipher(SecretKey secretKey, IvParameterSpec initializationVector) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(CYPHER_TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, initializationVector);
		return cipher;
	}
}
