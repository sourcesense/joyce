package com.sourcesense.nile.core.errors;

public class SchemaNotFoundException extends RuntimeException {
	public SchemaNotFoundException(String message){
		super(message);
	}
}
