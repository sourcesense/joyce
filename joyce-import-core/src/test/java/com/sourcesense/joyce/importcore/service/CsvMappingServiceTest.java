package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.service.CsvMappingService;
import com.sourcesense.joyce.importcore.test.ImportCoreJoyceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CsvMappingServiceTest extends ImportCoreJoyceTest {

	private CsvMappingService csvMappingService;

	@BeforeEach
	public void init() {
		csvMappingService = new CsvMappingService(csvMapper);
	}

	@Test
	public void shouldComputeDocumentsFromCsvFile() throws IOException, URISyntaxException {

		byte[] data = this.computeResourceAsByteArray("message/bulk/csv/01.csv");
		MultipartFile multipartFile = new MockMultipartFile("01", "01.csv", "text/csv", data);

		List<JsonNode> actual = csvMappingService.convertCsvFileToDocuments(multipartFile, ',', ";");
		List<JsonNode> expected = this.computeResourceAsObject("result/bulk/csv/01.json", new TypeReference<>() {});

		assertThat(expected).hasSameElementsAs(actual);
	}
}

