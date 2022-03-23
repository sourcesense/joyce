package com.sourcesense.joyce.importcore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.importcore.api.ImportRestApi;
import com.sourcesense.joyce.importcore.dto.BulkImportResult;
import com.sourcesense.joyce.importcore.dto.SingleImportResult;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import com.sourcesense.joyce.importcore.service.ImportService;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller that reads raw messages from request body and processes them.
 * There are two types of action that can be executed on a message: Insert and Delete.
 */
@RequiredArgsConstructor
public abstract class AbstractImportRestController implements ImportRestApi {

	protected final ImportService importService;
	protected final SchemaService schemaService;

	/**
	 * Insert raw message using schema found in the endpoint path.
	 *
	 * @param schemaId The key of the schema
	 * @param document The payload of the message
	 * @return true if the operation succeed
	 */
	@Override
	public SingleImportResult importDocument(String schemaId, ObjectNode document) {
		return importService.processImport(
				this.computeSingleRestRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
	}

	@Override
	public BulkImportResult importDocuments(
			String schemaId,
			MultipartFile data,
			Character columnSeparator,
			String arraySeparator) throws IOException {

		JoyceURI rawUri = this.computeBulkRestRawUri(data.getOriginalFilename());
		SchemaEntity schema = this.fetchSchema(schemaId);
		List<JsonNode> documents = importService.computeDocumentsFromFile(rawUri, data, columnSeparator, arraySeparator);

		Map<ProcessStatus, List<SingleImportResult>> results = documents.stream()
				.map(document -> this.importSingleDocument(rawUri, document, schema))
				.collect(Collectors.groupingBy(SingleImportResult::getProcessStatus));

		BulkImportResult finalReport = BulkImportResult.builder()
				.total(documents.size())
				.imported(this.computeCardinalityForProcessStatus(results, ProcessStatus.IMPORTED))
				.skipped(this.computeCardinalityForProcessStatus(results, ProcessStatus.SKIPPED))
				.failed(this.computeCardinalityForProcessStatus(results, ProcessStatus.FAILED))
				.build();

		return importService.notifyBulkImportSuccess(rawUri, finalReport);
	}

	/**
	 * Test endpoint that simulates an insert without posting a message on Kafka
	 *
	 * @param schemaId The key of the schema
	 * @param document The payload of the message
	 * @return Processed message
	 */
	@Override
	public SingleImportResult importDryRun(String schemaId, ObjectNode document) {
		return importService.importDryRun(
				this.computeSingleRestRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
	}

	/**
	 * Deletes raw message using schema found in the endpoint path.
	 *
	 * @param schemaId The key of the schema
	 * @param document The payload of the message
	 * @return true if the operation succeed
	 */
	@Override
	public SingleImportResult removeDocument(String schemaId, ObjectNode document) {
		return importService.removeDocument(
				this.computeSingleRestRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
	}


	private JoyceURI computeSingleRestRawUri() {
		String uuid = UUID.randomUUID().toString().substring(0, 6);
		long timestamp = new Date().toInstant().toEpochMilli();
		String uid = String.format("%d-%s", timestamp, uuid);
		return this.computeRawUri("single", uid);
	}

	private JoyceURI computeBulkRestRawUri(String fileName) {
		return this.computeRawUri("bulk", fileName);
	}

	private JoyceURI computeRawUri(String collection, String uid) {
		return JoyceURI.make(JoyceURI.Type.RAW, JoyceURI.Subtype.REST, collection, uid);
	}

	private SchemaEntity fetchSchema(String schemaUid) {
		return Optional.ofNullable(schemaUid)
				.flatMap(schemaService::get)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema %s does not exists", schemaUid)
				));
	}

	private SingleImportResult importSingleDocument(
			JoyceURI rawUri,
			JsonNode document,
			SchemaEntity schema) {

		try {
			return importService.processImport(rawUri, document, schema);

		} catch (Exception exception) {
			return SingleImportResult.builder().uri(rawUri).processStatus(ProcessStatus.FAILED).build();
		}
	}

	private Integer computeCardinalityForProcessStatus(
			Map<ProcessStatus, List<SingleImportResult>> results,
			ProcessStatus status) {

		return results.getOrDefault(status, new ArrayList<>()).size();
	}
}
