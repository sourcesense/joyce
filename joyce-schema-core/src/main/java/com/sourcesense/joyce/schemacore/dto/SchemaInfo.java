package com.sourcesense.joyce.schemacore.dto;

import com.sourcesense.joyce.core.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.model.JoyceURI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaInfo {

	private JoyceURI schemaUri;
	private List<ConnectorOperationStatus> connectors;
}
