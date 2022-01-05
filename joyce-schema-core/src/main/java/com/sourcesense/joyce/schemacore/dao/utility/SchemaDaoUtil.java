package com.sourcesense.joyce.schemacore.dao.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.SchemaEntity;

import java.util.Optional;

public interface SchemaDaoUtil {

	Optional<SchemaEntity> mapFromData(JsonNode json);
	<T> Optional<T> mapFromData(JsonNode json, Class<T> clazz);
}
