package com.sourcesense.joyce.core.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sourcesense.joyce.core.deserializer.ObjectToStringDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoyceSchemaMetadataConnector {

	private String name;

	@JsonDeserialize(using = ObjectToStringDeserializer.class)
	private String config;
}
