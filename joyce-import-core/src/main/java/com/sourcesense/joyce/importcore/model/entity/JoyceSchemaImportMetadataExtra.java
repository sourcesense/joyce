package com.sourcesense.joyce.importcore.model.entity;

import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadataExtra;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadataExtraConnector;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class JoyceSchemaImportMetadataExtra extends JoyceSchemaMetadataExtra {

	private TextNode filter;
	private List<JoyceSchemaMetadataExtraConnector> connectors;

	public JoyceSchemaImportMetadataExtra() {
		super();
	}

	public JoyceSchemaImportMetadataExtra(
			TextNode filter,
			List<JoyceSchemaMetadataExtraConnector> connectors) {

		super();
		this.filter = filter;
		this.connectors = connectors;
	}
}
