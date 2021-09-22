package com.sourcesense.joyce.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoyceSchemaMetadataExtraConnector {

	private String name;

	@JsonAlias({ "importKeyUid", "import-key-uid", "import_key_uid" })
	private String importKeyUid;

	private JsonNode config;
}
