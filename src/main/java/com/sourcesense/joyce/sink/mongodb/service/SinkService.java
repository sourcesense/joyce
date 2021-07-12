package com.sourcesense.joyce.sink.mongodb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.sourcesense.joyce.core.annotation.ContentUri;
import com.sourcesense.joyce.core.annotation.EventPayload;
import com.sourcesense.joyce.core.annotation.Notify;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.sink.mongodb.exception.MongodbSinkException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SinkService {

	private final ObjectMapper mapper;
	private final MongoTemplate mongoTemplate;


	/**
	 * This method saves processed document coming from kafka in mongodb
	 *
	 * @param message    Body of the processed document
	 * @param uri        Joyce uri of the processed document
	 * @param collection Mongodb collection
	 * @throws MongodbSinkException Exception thrown if there's an error in document saving
	 */
	@Notify(
			failureEvent = NotificationEvent.SINK_MONGODB_STORE_FAILED,
			successEvent = NotificationEvent.SINK_MONGODB_STORE_SUCCESS
	)
	public void saveDocument(
			@EventPayload ObjectNode message,
			@ContentUri JoyceURI uri,
			String collection) throws MongodbSinkException {

		Document doc = new Document(mapper.convertValue(message, new TypeReference<>() {
		}));
		doc.put("_id", uri.toString());
		Document res = mongoTemplate.save(doc, collection);
		if (res.isEmpty()) {
			throw new MongodbSinkException("Document was not saved, result is empty");
		}
	}


	/**
	 * This method deletes document already present on mongodb
	 *
	 * @param uri        Document uri
	 * @param collection Mongodb collection
	 * @throws MongodbSinkException Exception thrown if there's an error in document removal
	 */
	@Notify(
			failureEvent = NotificationEvent.SINK_MONGODB_STORE_FAILED,
			successEvent = NotificationEvent.SINK_MONGODB_DELETE_SUCCESS
	)
	public void deleteDocument(
			@ContentUri JoyceURI uri,
			String collection) throws MongodbSinkException {

		DeleteResult response;
		if (uri.getType().equals(JoyceURI.Type.RAW)) {
			response = mongoTemplate.remove(new Query(Criteria.where("_metadata_.raw_uri").is(uri.toString())), collection);
		} else { // if (uri.get().getType().equals(JoyceURI.Type.CONTENT)
			response = mongoTemplate.remove(new Query(Criteria.where("_id").is(uri.toString())), collection);
		}

		if (response.getDeletedCount() < 1) {
			throw new MongodbSinkException(String.format("There is no document to delete with uri %s", uri.toString()));
		}
	}

	/**
	 * This method converts document key in a joyceUri
	 *
	 * @param key Document key
	 * @return Joyce uri
	 * @throws MongodbSinkException Exception thrown if uri is invalid
	 */
	@Notify(failureEvent = NotificationEvent.SINK_MONGODB_STORE_FAILED)
	public JoyceURI getJoyceURI(@ContentUri String key) throws MongodbSinkException {
		return JoyceURI.createURI(key)
				.orElseThrow(
						() -> new MongodbSinkException(String.format("uri [%s] is not a Valid Joyce Uri", key))
				);
	}

	/**
	 * This method retrieves mongo collection from kafka headers
	 *
	 * @param key     Document key (Used for logging reasons)
	 * @param headers Kafka headers
	 * @return MongoDb collection
	 * @throws MongodbSinkException Exception thrown if header is missing
	 */
	@Notify(failureEvent = NotificationEvent.SINK_MONGODB_STORE_FAILED)
	public String getCollection(
			@ContentUri String key,
			Map<String, String> headers) throws MongodbSinkException {

		if (headers.get(KafkaCustomHeaders.COLLECTION) == null) {
			throw new MongodbSinkException(String.format("Missing %s header", KafkaCustomHeaders.COLLECTION));
		}
		return headers.get(KafkaCustomHeaders.COLLECTION);
	}
}
