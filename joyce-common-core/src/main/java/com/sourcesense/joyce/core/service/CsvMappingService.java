package com.sourcesense.joyce.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvMappingService {

	private final CsvMapper csvMapper;

	public List<JsonNode> convertCsvFileToDocuments(
			MultipartFile data,
			Character columnSeparator,
			String arraySeparator) throws IOException {

		CsvSchema csvSchema = this.buildSchema(
				csvMapper.schemaFor(JsonNode.class),
				columnSeparator,
				arraySeparator
		);

		MappingIterator<JsonNode> rowsIterator = csvMapper
				.readerFor(JsonNode.class)
				.with(csvSchema)
				.readValues(data.getBytes());

		List<JsonNode> documents = new ArrayList<>();
		rowsIterator.forEachRemaining(documents::add);
		return documents;
	}

	public String convertArrayToCsv(
			ArrayNode array,
			Character columnSeparator,
			String arraySeparator) throws JsonProcessingException {

		if (array.isEmpty()) {
			return StringUtils.EMPTY;
		}

		CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
		array.get(0).fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);

		CsvSchema csvSchema = this.buildSchema(csvSchemaBuilder.build(), columnSeparator, arraySeparator);

		return csvMapper.writerFor(JsonNode.class)
				.with(csvSchema)
				.writeValueAsString(array);
	}

	private CsvSchema buildSchema(
			CsvSchema csvSchema,
			Character columnSeparator,
			String arraySeparator) {

		return csvSchema.withHeader()
				.withColumnSeparator(columnSeparator != null ? columnSeparator : ',')
				.withArrayElementSeparator(arraySeparator != null ? arraySeparator : ";");
	}
}
