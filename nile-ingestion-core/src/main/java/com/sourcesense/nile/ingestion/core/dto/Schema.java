package com.sourcesense.nile.ingestion.core.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Schema {
	private String uid;
	private Integer version;
	private String name;
	private String description;
	private Map schema;

}
