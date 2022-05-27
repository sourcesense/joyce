package com.sourcesense.joyce.schemacore.model.entity;

import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SchemaMetadata {

	private String uidKey;
	private String type;
	private String domain;
	private String product;
	private String name;
	private String description;
	private JoyceSchemaURI parent;
	private List<Map<String, Object>> indexes;

	private Boolean production;
	private Boolean store;
	private Boolean validation;
	private Boolean indexed;

	private String data;
	private Map<String, Object> extra;
}
