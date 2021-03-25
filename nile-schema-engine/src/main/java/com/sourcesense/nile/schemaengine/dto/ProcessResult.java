package com.sourcesense.nile.schemaengine.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class ProcessResult {
	private final JsonNode json;
	private final Optional<Map<String, Object>> context;
}
