package com.sourcesense.nile.schemaengine.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("nile.schemaengine")
@Getter @Setter
@Component
public class SchemaEngineProperties {
	/**
	 * Load schema as yaml instead of json
	 */
	private Boolean yaml   = false;

	public void setHandlers(Map<String, String> handlers) {
		this.handlers = handlers;
	}

	/**
	 * Map of handlers to load
	 * the key is the key that you want to implement in the json-schema
	 * tha value is a Bean name that you provide as an injectable Component
	 */
	private Map<String, String> handlers = new HashMap<>();
}
