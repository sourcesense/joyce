package com.sourcesense.joyce.schemaengine.service;

import com.sourcesense.joyce.schemaengine.configuration.CryptingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CryptServiceTest {

	private CryptingService cryptingService;

	private final static String TEST_SECRET_KEY = "F985C96FEC5150A02BF1F889245B93C9BA1EDF440865175911A97A6A9D819AB9";
	private final static String TEST_PLAINTEXT_PASSWORD = "quanto fa 3*3?";
	private final static String TEST_ENCRYPTED_PASSWORD = "cAJGp/F6h/h6ItuXkVADCF8OWfr5oKVvScvKiubKNpo=";

	@BeforeEach
	public void init() throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		CryptingConfig cryptingConfig = new CryptingConfig(TEST_SECRET_KEY);
		SecretKey secretKey = cryptingConfig.secretKey();
		cryptingService = new CryptingService(secretKey);
	}

	@Test
	public void shouldDecryptPassword() {
		assertEquals(
				TEST_PLAINTEXT_PASSWORD,
				cryptingService.decrypt(TEST_ENCRYPTED_PASSWORD)
		);
	}
}
