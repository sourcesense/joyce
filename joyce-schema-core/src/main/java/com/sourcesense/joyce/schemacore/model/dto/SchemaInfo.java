package com.sourcesense.joyce.schemacore.model.dto;

import com.sourcesense.joyce.core.model.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
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

	private JoyceSchemaURI schemaUri;
	private List<ConnectorOperationStatus> connectors;
}
