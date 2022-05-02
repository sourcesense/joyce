package com.sourcesense.joyce.schemaengine.configuration;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class CryptingConfig {

	private final String secretKey;

	private final static String SECRET_KEY_ALGORITHM = "AES";

	public CryptingConfig(@Value("${joyce.crypter.secret-key}") String secretKey) {
		this.secretKey = secretKey;
	}

	@Bean
	public SecretKey secretKey() {
		return new SecretKeySpec(HexUtils.fromHexString(secretKey), SECRET_KEY_ALGORITHM);
	}
}
