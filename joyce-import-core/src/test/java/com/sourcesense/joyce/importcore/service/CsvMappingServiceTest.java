package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.sourcesense.joyce.core.service.CsvMappingService;
import com.sourcesense.joyce.importcore.test.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CsvMappingServiceTest implements TestUtility {

	private ObjectMapper objectMapper;
	private CsvMappingService csvMappingService;

	@BeforeEach
	public void init() {
		CsvMapper csvMapper = this.initCsvMapper();
		objectMapper = new ObjectMapper();
		csvMappingService = new CsvMappingService(csvMapper);
	}

	@Test
	public void shouldComputeDocumentsFromCsvFile() throws IOException, URISyntaxException {

		byte[] data = this.computeResourceAsByteArray("message/bulk/csv/01.csv");
		MultipartFile multipartFile = new MockMultipartFile("01", "01.csv", "text/csv", data);

		List<JsonNode> actual = csvMappingService.convertCsvFileToDocuments(multipartFile, ',', ";");
		List<JsonNode> expected = this.computeResourceAsNodeList("result/bulk/csv/01.json");

		assertThat(expected).hasSameElementsAs(actual);
	}

	private List<JsonNode> computeResourceAsNodeList(String path) throws IOException {
		return objectMapper.readValue(
				this.computeResourceAsBytes(path),
				new TypeReference<>() {}
		);
	}
}

