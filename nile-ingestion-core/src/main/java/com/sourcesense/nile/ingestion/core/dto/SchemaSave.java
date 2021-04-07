package com.sourcesense.nile.ingestion.core.dto;

import lombok.Data;

@Data
public class SchemaSave {
	private String name;
	private String schema;
	private String description;
}
