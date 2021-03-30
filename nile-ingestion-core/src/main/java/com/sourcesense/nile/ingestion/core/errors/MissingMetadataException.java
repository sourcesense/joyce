package com.sourcesense.nile.ingestion.core.errors;

public class MissingMetadataException extends RuntimeException {
	public MissingMetadataException(String s) {
		super(s);
	}
}
