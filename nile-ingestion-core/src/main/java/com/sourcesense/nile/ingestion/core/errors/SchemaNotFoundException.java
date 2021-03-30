package com.sourcesense.nile.ingestion.core.errors;

public class SchemaNotFoundException extends RuntimeException {
	public SchemaNotFoundException(String message){
		super(message);
	}
}
