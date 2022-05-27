package com.sourcesense.joyce.core.model.entity;

import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;

public interface JoyceSchema {

	JoyceSchemaURI getUid();
	JoyceSchemaMetadata getMetadata();
}
