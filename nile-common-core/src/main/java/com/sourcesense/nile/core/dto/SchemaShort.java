package com.sourcesense.nile.core.dto;

import lombok.Data;


@Data
public class SchemaShort {
	private String uid;
	private Integer version;
	private String description;
	private Boolean development;
}
