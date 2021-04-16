package com.sourcesense.nile.ingestion.core.errors;

public class IngestionException extends RuntimeException {
	public IngestionException(String mess){
		super(mess);
	}
}
