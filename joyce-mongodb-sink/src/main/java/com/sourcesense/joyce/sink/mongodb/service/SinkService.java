package com.sourcesense.joyce.sink.mongodb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.sourcesense.joyce.core.annotation.DocumentUri;
import com.sourcesense.joyce.core.annotation.EventPayload;
import com.sourcesense.joyce.core.annotation.Notify;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.uri.JoyceDocumentURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.sink.mongodb.exception.MongodbSinkException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SinkService {

	private final ObjectMapper mapper;
	private final MongoTemplate mongoTemplate;

	/**
	 * This method saves processed document coming from kafka in mongodb
	 *
	 * @param message     Body of the processed document
	 * @param documentURI Joyce uri of the processed document
	 * @param collection  Mongodb collection
	 * @throws MongodbSinkException Exception thrown if there's an error in document saving
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_STORE_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_STORE_FAILED
	)
	public void saveDocument(@EventPayload ObjectNode message, @DocumentUri String documentURI, String collection) throws MongodbSinkException {

		this.validateCollection(collection);
		JoyceDocumentURI joyceDocumentURI = JoyceURIFactory.getInstance().createURIOrElseThrow(documentURI, JoyceDocumentURI.class);

		Document document = new Document(mapper.convertValue(message, new TypeReference<>() {}));
		document.put("_id", joyceDocumentURI.toString());
		Document response = mongoTemplate.save(document, collection);

		if (response.isEmpty()) {
			throw new MongodbSinkException("Document was not saved, result is empty");
		}
	}

	/**
	 * This method deletes document already present on mongodb
	 *
	 * @param documentURI Document uri
	 * @param collection  Mongodb collection
	 * @throws MongodbSinkException Exception thrown if there's an error in document removal
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_DELETE_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_DELETE_FAILED
	)
	public void deleteDocument(@DocumentUri String documentURI, String collection) throws MongodbSinkException {

		this.validateCollection(collection);
		Query query = new Query(Criteria.where("_id").is(documentURI));
		DeleteResult response = mongoTemplate.remove(query, collection);

		if (response.getDeletedCount() < 1) {
			throw new MongodbSinkException(String.format("There is no document to delete with uri %s", documentURI));
		}
	}

	/**
	 * This method retrieves mongo collection from kafka headers
	 *
	 * @param collection MongoDb collection retrieved from Kafka headers
	 * @throws MongodbSinkException Exception thrown if header is missing
	 */
	private void validateCollection(String collection) throws MongodbSinkException {
		if (Objects.isNull(collection)) {
			throw new MongodbSinkException(
					String.format("Impossible to complete sink operation, missing %s header", KafkaCustomHeaders.COLLECTION)
			);
		}
	}
}
