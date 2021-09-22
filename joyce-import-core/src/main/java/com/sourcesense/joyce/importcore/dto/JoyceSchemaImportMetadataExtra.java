package com.sourcesense.joyce.importcore.dto;

import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtra;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtraConnector;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class JoyceSchemaImportMetadataExtra extends JoyceSchemaMetadataExtra {

	private List<JoyceSchemaMetadataExtraConnector> connectors;

	public JoyceSchemaImportMetadataExtra() {
		super();
	}

	public JoyceSchemaImportMetadataExtra(List<JoyceSchemaMetadataExtraConnector> connectors) {
		super();
		this.connectors = connectors;
	}
}
