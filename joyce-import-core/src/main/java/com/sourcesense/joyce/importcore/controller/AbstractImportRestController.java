package com.sourcesense.joyce.importcore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIChannel;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIRestOrigin;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.importcore.api.ImportRestApi;
import com.sourcesense.joyce.importcore.dto.BulkImportResult;
import com.sourcesense.joyce.importcore.dto.SingleImportResult;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import com.sourcesense.joyce.importcore.service.ImportService;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.TriFunction;
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
		return this.executeSingleImportOperation(schemaId, document, importService::processImport);
	}

	@Override
	public BulkImportResult importDocuments(
			String schemaId,
			MultipartFile data,
			Character columnSeparator,
			String arraySeparator) throws IOException {

		SchemaEntity schema = this.fetchSchema(schemaId);
		JoyceSourceURI sourceURI = JoyceURIFactory.getInstance().createSourceURIOrElseThrow(
				schema.getUid().getDomain(),
				schema.getUid().getProduct(),
				schema.getUid().getName(),
				JoyceURIChannel.REST,
				JoyceURIRestOrigin.BULK,
				data.getOriginalFilename()
		);

		List<JsonNode> documents = importService.computeDocumentsFromFile(sourceURI, data, columnSeparator, arraySeparator);

		Map<ProcessStatus, List<SingleImportResult>> results = documents.stream()
				.map(document -> this.importSingleDocument(sourceURI, document, schema))
				.collect(Collectors.groupingBy(SingleImportResult::getProcessStatus));

		BulkImportResult finalReport = BulkImportResult.builder()
				.total(documents.size())
				.imported(this.computeOccurrencesForProcessStatus(results, ProcessStatus.IMPORTED))
				.skipped(this.computeOccurrencesForProcessStatus(results, ProcessStatus.SKIPPED))
				.failed(this.computeOccurrencesForProcessStatus(results, ProcessStatus.FAILED))
				.build();

		return importService.notifyBulkImportSuccess(sourceURI, finalReport);
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
		return this.executeSingleImportOperation(schemaId, document, importService::importDryRun);
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
		return this.executeSingleImportOperation(schemaId, document, importService::removeDocument);
	}

	private SingleImportResult executeSingleImportOperation(
			String schemaId,
			ObjectNode document,
			TriFunction<JoyceSourceURI, ObjectNode, SchemaEntity, SingleImportResult> operation) {

		SchemaEntity schema = this.fetchSchema(schemaId);
		JoyceSourceURI sourceURI = this.computeSingleRestSourceUri(
				schema.getMetadata().getDomain(),
				schema.getMetadata().getProduct(),
				schema.getMetadata().getName()
		);
		return operation.apply(sourceURI, document, schema);
	}

	private JoyceSourceURI computeSingleRestSourceUri(String domain, String product, String name) {
		String uuid = UUID.randomUUID().toString().substring(0, 6);
		long timestamp = new Date().toInstant().toEpochMilli();
		String uid = String.format("%d-%s", timestamp, uuid);
		return JoyceURIFactory.getInstance().createSourceURIOrElseThrow(
				domain, product, name, JoyceURIChannel.REST, JoyceURIRestOrigin.SINGLE, uid
		);
	}

	private SchemaEntity fetchSchema(String schemaUid) {
		return Optional.ofNullable(schemaUid)
				.flatMap(schemaService::get)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema %s does not exists", schemaUid)
				));
	}

	private SingleImportResult importSingleDocument(
			JoyceSourceURI sourceURI,
			JsonNode document,
			SchemaEntity schema) {

		try {
			return importService.processImport(sourceURI, document, schema);

		} catch (Exception exception) {
			return SingleImportResult.builder()
					.uri(sourceURI)
					.processStatus(ProcessStatus.FAILED)
					.build();
		}
	}

	private Integer computeOccurrencesForProcessStatus(
			Map<ProcessStatus, List<SingleImportResult>> results,
			ProcessStatus status) {

		return results.getOrDefault(status, new ArrayList<>()).size();
	}
}
