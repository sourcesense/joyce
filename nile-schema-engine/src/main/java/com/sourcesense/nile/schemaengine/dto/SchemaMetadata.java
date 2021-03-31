package com.sourcesense.nile.schemaengine.dto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class SchemaMetadata {

	final private Map<String, Object> metadata;

	public Object get(String key){
		return metadata.get(key);
	}
	public Map<String, Object> getAll() {
		return metadata;
	}
}
