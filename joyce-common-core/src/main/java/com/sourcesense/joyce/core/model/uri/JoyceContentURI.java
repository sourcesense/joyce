package com.sourcesense.joyce.core.model.uri;

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

	public JoyceSchemaURI getSchemaURI() {
		return JoyceURIFactory.getInstance().createSchemaURIOrElseThrow(domain, product, name);
	}

	public String getCollection() {
		return String.format("%s-%s-%s", domain, product, name);
	}
}
