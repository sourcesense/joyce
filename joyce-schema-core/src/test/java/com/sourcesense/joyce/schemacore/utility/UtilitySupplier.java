package com.sourcesense.joyce.schemacore.utility;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sourcesense.joyce.core.exception.TestRuntimeException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public interface UtilitySupplier {

	default ObjectMapper initJsonMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	default byte[] getResourceAsBytes(String path) {
		try {
			URL res = this.getClass().getClassLoader().getResource(path);
			return Files.readAllBytes(Path.of(res.toURI()));

		} catch (Exception exception) {
			throw new TestRuntimeException("Unable to load resource", exception);
		}
	}

	default String getResourceAsString(String path) {
		try {
			URL res = this.getClass().getClassLoader().getResource(path);
			return Files.readString(Path.of(res.toURI()));

		} catch (Exception exception) {
			throw new TestRuntimeException("Unable to load resource", exception);
		}
	}
}
