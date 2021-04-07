package com.sourcesense.nile.core.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SchemaSave {
	private String name;
	private Map schema;
	private String description;
}
