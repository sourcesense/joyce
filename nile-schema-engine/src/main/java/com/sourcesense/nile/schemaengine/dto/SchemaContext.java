package com.sourcesense.nile.schemaengine.dto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class SchemaContext {
	
	final private Map<String, Object> context;

	public Object get(String key){
		return context.get(key);
	}
	public Map<String, Object> getContext() {
		return context;
	}
}
