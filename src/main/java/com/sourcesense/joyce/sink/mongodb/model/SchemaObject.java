package com.sourcesense.joyce.sink.mongodb.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sourcesense.joyce.sink.mongodb.deserializer.TypeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;


/**
 *
 * This model is used to normalize schema for mongodb json schema validation
 * using jackson objectMapper convertValue method and annotations.
 *
 * Mongodb validator must have the following restrictions:
 * 1)Type must be called bsonType
 * 2)Required field must be non empty
 * 3)Custom transformer handlers must be stripped
 * 4)Primitive bson type names must be normalized to conform mongodb specs
 */
@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class SchemaObject {

	@JsonDeserialize(using = TypeDeserializer.class)
	@JsonAlias(value = "type")
	private String bsonType;

	@JsonInclude(Include.NON_EMPTY)
	private List<String> required;

	@JsonInclude(Include.NON_NULL)
	private SchemaObject items;

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, SchemaObject> properties;
}
