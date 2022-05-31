package com.sourcesense.joyce.test.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.sourcesense.joyce.test.exception.TestException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public abstract class JoyceTest {

	protected final CsvMapper csvMapper;
	protected final YAMLMapper yamlMapper;
	protected final ObjectMapper jsonMapper;

	protected abstract void setupMappers(CsvMapper csvMapper, YAMLMapper yamlMapper, ObjectMapper jsonMapper);

	protected JoyceTest() {
		this.csvMapper = this.initCsvMapper();
		this.yamlMapper = this.initYamlMapper();
		this.jsonMapper = this.initJsonMapper();
		this.setupMappers(csvMapper, yamlMapper, jsonMapper);
	}

	protected  <T> T computeResourceAsObject(String path, Class<T> clazz) throws IOException, URISyntaxException {
		byte[] resource = this.computeResourceAsByteArray(path);
		return jsonMapper.readValue(resource, clazz);
	}

	protected <T> T computeResourceAsObject(String path, TypeReference<T> type) throws IOException, URISyntaxException {
		byte[] resource = this.computeResourceAsByteArray(path);
		return jsonMapper.readValue(resource, type);
	}

	protected JsonNode computeResourceAsNode(String path) throws IOException, URISyntaxException {
		byte[] resource = this.computeResourceAsByteArray(path);
		return jsonMapper.readTree(resource);
	}

	protected InputStream computeResourceAsBytes(String jsonFileName) {
		return this.getClass().getClassLoader().getResourceAsStream(jsonFileName);
	}

	protected byte[] computeResourceAsByteArray(String path) throws IOException, URISyntaxException {
		return Files.readAllBytes(this.computeResourcePath(path));
	}

	protected String computeResourceAsString(String path) throws IOException, URISyntaxException {
		return Files.readString(this.computeResourcePath(path));
	}

	protected Path computeResourcePath(String path) throws URISyntaxException {
		URL resourceURL = this.getClass().getClassLoader().getResource(path);
		if(Objects.nonNull(resourceURL)) {
			return Path.of(resourceURL.toURI());

		} else {
			throw new TestException("Invalid resource path");
		}
	}

	private CsvMapper initCsvMapper() {
		return new CsvMapper()
				.enable(CsvParser.Feature.TRIM_SPACES)
				.enable(CsvParser.Feature.ALLOW_COMMENTS)
				.enable(CsvParser.Feature.ALLOW_TRAILING_COMMA)
				.enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
				.enable(CsvParser.Feature.SKIP_EMPTY_LINES);
	}

	private YAMLMapper initYamlMapper() {
		YAMLMapper yamlMapper = new YAMLMapper();
		yamlMapper.disable(YAMLGenerator.Feature.SPLIT_LINES);
		yamlMapper.enable(YAMLGenerator.Feature.INDENT_ARRAYS);
		yamlMapper.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
		yamlMapper.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
		yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		return yamlMapper;
	}

	private ObjectMapper initJsonMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}
}
