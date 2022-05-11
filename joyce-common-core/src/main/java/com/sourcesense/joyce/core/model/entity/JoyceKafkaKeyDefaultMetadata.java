package com.sourcesense.joyce.core.model.entity;

import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoyceKafkaKeyDefaultMetadata implements JoyceKafkaKeyMetadata {

	private JoyceSourceURI sourceURI;
	private JoyceSchemaURI parentURI;
	private Boolean store;
}
