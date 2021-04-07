package com.sourcesense.nile.core.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class SchemaEntity {
	@Id
	private String uid;
	private Integer version;
	private String name;
	private String description;
	private String schema;

}
