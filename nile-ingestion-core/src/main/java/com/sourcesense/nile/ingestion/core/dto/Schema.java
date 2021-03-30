package com.sourcesense.nile.ingestion.core.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Schema {
	private String uid;
	private Map schema;
	private String description;
}
