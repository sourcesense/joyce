package com.sourcesense.joyce.sink.mongodb.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sourcesense.joyce.sink.mongodb.serializer.TypeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class SchemaObject {

	@JsonDeserialize(using = TypeSerializer.class)
	@JsonAlias(value = "type")
	private String bsonType;

	@JsonInclude(Include.NON_EMPTY)
	private List<String> required;

	@JsonInclude(Include.NON_NULL)
	private SchemaObject items;

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, SchemaObject> properties;
}
