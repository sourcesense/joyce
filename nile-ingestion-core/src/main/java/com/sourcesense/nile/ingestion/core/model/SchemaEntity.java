package com.sourcesense.nile.ingestion.core.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
public class SchemaEntity {
	@Id
	private String uid;
	private String schema;
}
