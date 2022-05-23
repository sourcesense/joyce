package com.sourcesense.joyce.core.model.entity;

import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import lombok.*;


@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JoyceKafkaKeyDefaultMetadata extends JoyceKafkaKeyMetadata {

	private JoyceSourceURI sourceURI;
	private JoyceSchemaURI parentURI;
	private Boolean store;

	@Builder
	public JoyceKafkaKeyDefaultMetadata(String schemaType, JoyceSchemaURI schemaURI, JoyceSourceURI sourceURI, JoyceSchemaURI parentURI, Boolean store) {
		super(schemaType, schemaURI);
		this.sourceURI = sourceURI;
		this.parentURI = parentURI;
		this.store = store;
	}
}
