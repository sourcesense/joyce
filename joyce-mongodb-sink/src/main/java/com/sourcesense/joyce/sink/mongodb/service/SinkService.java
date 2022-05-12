package com.sourcesense.joyce.sink.mongodb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.sourcesense.joyce.core.annotation.ContentURI;
import com.sourcesense.joyce.core.annotation.EventPayload;
import com.sourcesense.joyce.core.annotation.Notify;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIContentType;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyMetadata;
import com.sourcesense.joyce.core.model.uri.JoyceContentURI;
import com.sourcesense.joyce.core.service.ConsumerService;
import com.sourcesense.joyce.sink.mongodb.exception.MongodbSinkException;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SinkService extends ConsumerService {

	private final MongoTemplate mongoTemplate;

	public SinkService(ObjectMapper jsonMapper, MongoTemplate mongoTemplate) {
		super(jsonMapper);
		this.mongoTemplate = mongoTemplate;
	}

	@Notify(failureEvent = NotificationEvent.SINK_MONGODB_ERROR_INVALID_MESSAGE_KEY)
	public JoyceKafkaKey<JoyceContentURI, JoyceKafkaKeyDefaultMetadata> computeJoyceKafkaKey(@ContentURI String messageKey) throws JsonProcessingException {
		return super.computeKafkaKey(messageKey, JoyceContentURI.class, JoyceKafkaKeyDefaultMetadata.class);
	}

	/**
	 * This method saves processed document coming from kafka in mongodb
	 *
	 * @param message     Body of the processed document
	 * @param contentURI Joyce uri of the processed document
	 * @throws MongodbSinkException Exception thrown if there's an error in document saving
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_STORE_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_STORE_FAILED
	)
	public void saveDocument(
			@EventPayload ObjectNode message,
			@ContentURI JoyceContentURI contentURI,
			String collection) throws MongodbSinkException {

		Map<String, Object> document = jsonMapper.convertValue(message, new TypeReference<>() {});
		document.put("_id", contentURI.toString());
		Document response = mongoTemplate.save(new Document(document), collection);

		if (response.isEmpty()) {
			throw new MongodbSinkException("Document was not saved, result is empty");
		}
	}

	/**
	 * This method deletes document already present on mongodb
	 *
	 * @param contentURI Document uri
	 * @throws MongodbSinkException Exception thrown if there's an error in document removal
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_DELETE_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_DELETE_FAILED
	)
	public void deleteDocument(@ContentURI JoyceContentURI contentURI, String collection) throws MongodbSinkException {
		String uriMatchKey = JoyceURIContentType.SOURCE.equalsIgnoreCase(contentURI.getContentType()) ? "_metadata_.source_uri" : "_id";
		Query query = new Query(Criteria.where(uriMatchKey).is(contentURI));
		DeleteResult response = mongoTemplate.remove(query, collection);

		if (response.getDeletedCount() < 1) {
			throw new MongodbSinkException(String.format("There is no document to delete with uri %s", contentURI));
		}
	}
}
