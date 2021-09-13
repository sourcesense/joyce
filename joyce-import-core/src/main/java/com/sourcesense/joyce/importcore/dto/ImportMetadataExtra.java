package com.sourcesense.joyce.importcore.dto;

import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtra;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportMetadataExtra extends JoyceSchemaMetadataExtra {

	public ImportMetadataExtra() {
		super();
	}
}
