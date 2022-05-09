package com.sourcesense.joyce.core.model.uri;

import com.sourcesense.joyce.core.enumeration.uri.JoyceURIContentType;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIKind;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JoyceContentURI extends JoyceTaxonomyURI {

	protected final String contentType;

	protected JoyceContentURI(URI uri, String kind, String name, String domain, String product, String contentType) {
		super(uri, kind, name, domain, product);
		this.contentType = contentType;
	}

	public String getSchemaUri() {
		return String.format("%s:%s:%s:%s:%s:%s", URI_SCHEMA, JoyceURIKind.CONTENT, domain, product, name, JoyceURIContentType.SCHEMA);
	}

	public String getCollection() {
		return String.format("%s-%s-%s", domain, product, name);
	}
}
