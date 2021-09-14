package com.sourcesense.joyce.importcore.dto;

import com.sourcesense.joyce.core.model.JoyceSchemaMetadataConnector;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtra;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportMetadataExtra extends JoyceSchemaMetadataExtra {

	private List<JoyceSchemaMetadataConnector> connect;

	public ImportMetadataExtra() {
		super();
	}

	public ImportMetadataExtra(List<JoyceSchemaMetadataConnector> connect) {
		super();
		this.connect = connect;
	}
}
