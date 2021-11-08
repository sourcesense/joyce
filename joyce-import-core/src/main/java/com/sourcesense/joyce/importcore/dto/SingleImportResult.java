package com.sourcesense.joyce.importcore.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.JoyceURI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SingleImportResult {

	private JoyceURI uri;
	private boolean processed;
	private JsonNode result;
}
