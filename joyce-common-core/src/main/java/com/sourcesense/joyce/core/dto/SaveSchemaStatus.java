package com.sourcesense.joyce.core.dto;

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
public class SaveSchemaStatus {

	private JoyceURI schemaUri;
	private List<ConnectorUpdateStatus> connectors;
}
