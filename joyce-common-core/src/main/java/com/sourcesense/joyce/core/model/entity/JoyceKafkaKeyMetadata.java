package com.sourcesense.joyce.core.model.entity;

import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class JoyceKafkaKeyMetadata {

	private String schemaType;
	private JoyceSchemaURI schemaURI;

}
