package com.sourcesense.joyce.core.model.uri;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JoyceSchemaURI extends JoyceContentURI{

	protected JoyceSchemaURI(URI uri, String kind, String name, String domain, String product, String contentType) {
		super(uri, kind, name, domain, product, contentType);
	}
}
