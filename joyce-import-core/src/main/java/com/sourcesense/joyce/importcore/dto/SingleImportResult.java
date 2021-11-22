package com.sourcesense.joyce.importcore.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SingleImportResult {

	private JoyceURI uri;
	private ProcessStatus processStatus;
	private JsonNode result;
}
