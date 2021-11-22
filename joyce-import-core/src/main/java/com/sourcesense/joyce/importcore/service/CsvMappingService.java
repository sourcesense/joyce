package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvMappingService {

	private final CsvMapper csvMapper;

	public List<JsonNode> computeDocumentsFromCsvFile(
			MultipartFile data,
			Character columnSeparator,
			String arraySeparator) throws IOException {

		CsvSchema csvSchema = this.buildSchema(columnSeparator, arraySeparator);

		MappingIterator<JsonNode> rowsIterator = csvMapper
				.readerFor(JsonNode.class)
				.with(csvSchema)
				.readValues(data.getBytes());

		List<JsonNode> documents = new ArrayList<>();
		rowsIterator.forEachRemaining(documents::add);
		return documents;
	}

	private CsvSchema buildSchema(Character columnSeparator, String arraySeparator) {
		return csvMapper.schemaFor(JsonNode.class)
				.withHeader()
				.withColumnSeparator(columnSeparator)
				.withArrayElementSeparator(arraySeparator);
	}
}
