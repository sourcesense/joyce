package com.sourcesense.joyce.importcore.dto;

import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtra;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class JoyceSchemaImportMetadataExtra extends JoyceSchemaMetadataExtra {

	private List<JoyceSchemaImportMetadataExtraConnector> connectors;

	public JoyceSchemaImportMetadataExtra() {
		super();
	}

	public JoyceSchemaImportMetadataExtra(List<JoyceSchemaImportMetadataExtraConnector> connectors) {
		super();
		this.connectors = connectors;
	}
}
