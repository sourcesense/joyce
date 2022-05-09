package com.sourcesense.joyce.importcore.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SingleImportResult {

	private JoyceSourceURI uri;
	private ProcessStatus processStatus;
	private JsonNode result;
}
