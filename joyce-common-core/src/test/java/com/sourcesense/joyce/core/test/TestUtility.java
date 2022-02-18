package com.sourcesense.joyce.core.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public interface TestUtility {

	ObjectMapper jsonMapper = initJsonMapper();

	static ObjectMapper initJsonMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	default Struct computeStruct(String json) throws InvalidProtocolBufferException {
		Struct.Builder structBuilder = Struct.newBuilder();
		JsonFormat.parser().ignoringUnknownFields().merge(json, structBuilder);
		return structBuilder.build();
	}

	default <T> T computeResourceAsObject(String path, Class<T> clazz) throws IOException, URISyntaxException {
		byte[] resource = this.computeResourceAsByteArray(path);
		return jsonMapper.readValue(resource, clazz);
	}

	default <T> T computeResourceAsObject(String path, TypeReference<T> type) throws IOException, URISyntaxException {
		byte[] resource = this.computeResourceAsByteArray(path);
		return jsonMapper.readValue(resource, type);
	}

	default InputStream computeResourceAsBytes(String jsonFileName) {
		return this.getClass().getClassLoader().getResourceAsStream(jsonFileName);
	}

	default byte[] computeResourceAsByteArray(String path) throws IOException, URISyntaxException {
		URL url = this.getClass().getClassLoader().getResource(path);
		return Files.readAllBytes(Path.of(url.toURI()));
	}

	default String computeResourceAsString(String path) throws IOException, URISyntaxException {
		URL url = this.getClass().getClassLoader().getResource(path);
		return Files.readString(Path.of(url.toURI()));
	}
}
