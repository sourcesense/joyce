package com.sourcesense.nile.ingestion.core.dto;

import lombok.Data;

@Data
public class SchemaSave {
	private String uid;
	private String schema;
	private String description;
}
