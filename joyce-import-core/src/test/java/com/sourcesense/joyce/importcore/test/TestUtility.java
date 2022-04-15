package com.sourcesense.joyce.importcore.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.sourcesense.joyce.core.configuration.jackson.JacksonMappersModuleRegisterer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public interface TestUtility {

	ObjectMapper jsonMapper = initJsonMapper();

	static ObjectMapper initJsonMapper() {
		ObjectMapper jsonMapper = buildJsonMapper();
		JacksonMappersModuleRegisterer moduleRegisterer = new JacksonMappersModuleRegisterer(jsonMapper, new YAMLMapper());
		moduleRegisterer.registerJoyceURIModule();
		return jsonMapper;
	}

	static ObjectMapper buildJsonMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	default CsvMapper initCsvMapper() {
		return new CsvMapper()
				.enable(CsvParser.Feature.TRIM_SPACES)
				.enable(CsvParser.Feature.ALLOW_COMMENTS)
				.enable(CsvParser.Feature.ALLOW_TRAILING_COMMA)
				.enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
				.enable(CsvParser.Feature.SKIP_EMPTY_LINES);
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
