package com.sourcesense.nile.ingestion.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
	private String message;
	private String error;
}
