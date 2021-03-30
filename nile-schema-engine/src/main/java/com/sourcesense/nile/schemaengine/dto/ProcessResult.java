package com.sourcesense.nile.schemaengine.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class ProcessResult {
	private final Map json;
	private final Optional<SchemaMetadata> metadata;
}
