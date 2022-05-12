package com.sourcesense.joyce.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.enumeration.JoyceAction;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyMetadata;
import com.sourcesense.joyce.core.model.uri.JoyceContentURI;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class ConsumerService {

	protected final ObjectMapper jsonMapper;

	/**
	 * Parses the kafkaKey into a java object
	 *
	 * @param messageKey the key associated with the consumed kafka message
	 * @return Object containing the converted kafkaKey
	 */
	public <J extends JoyceContentURI, M extends JoyceKafkaKeyMetadata> JoyceKafkaKey<J, M> computeKafkaKey(
			String messageKey,
			Class<J> joyceUriClass,
			Class<M> joyceKafkaKeyMetadataClass) throws JsonProcessingException {

		JavaType joyceURIType = jsonMapper.getTypeFactory().constructType(joyceUriClass);
		JavaType joyceKafkaKeyMetadataType = jsonMapper.getTypeFactory().constructType(joyceKafkaKeyMetadataClass);
		JavaType joyceKafkaKeyType = jsonMapper.getTypeFactory().constructParametricType(JoyceKafkaKey.class, joyceURIType, joyceKafkaKeyMetadataType);
		return jsonMapper.readValue(messageKey, joyceKafkaKeyType);
	}

	public <J extends JoyceContentURI, M extends JoyceKafkaKeyMetadata> JoyceAction computeAction(JoyceKafkaKey<J, M> kafkaKey) {
		return Optional.ofNullable(kafkaKey.getAction()).orElse(JoyceAction.INSERT);
	}

	public <J extends JoyceContentURI> boolean computeStoreContent(JoyceKafkaKey<J, JoyceKafkaKeyDefaultMetadata> kafkaKey) {
		return Optional.ofNullable(kafkaKey.getMetadata())
				.map(JoyceKafkaKeyDefaultMetadata::getStore)
				.orElse(true);
	}

	public <J extends JoyceContentURI> boolean keyContainsParent(JoyceKafkaKey<J, JoyceKafkaKeyDefaultMetadata> kafkaKey) {
		return Optional.of(kafkaKey)
				.map(JoyceKafkaKey::getMetadata)
				.map(JoyceKafkaKeyDefaultMetadata::getParentURI)
				.isPresent();
	}

	public <J extends JoyceContentURI> String computeCollection(JoyceKafkaKey<J, JoyceKafkaKeyDefaultMetadata> kafkaKey) {
		return Optional.of(kafkaKey)
				.map(JoyceKafkaKey::getMetadata)
				.map(JoyceKafkaKeyDefaultMetadata::getParentURI)
				.map(JoyceContentURI.class::cast)
				.orElseGet(kafkaKey::getUri)
				.getCollection();
	}
}
