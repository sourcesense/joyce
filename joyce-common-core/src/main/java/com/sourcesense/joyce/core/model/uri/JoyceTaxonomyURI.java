package com.sourcesense.joyce.core.model.uri;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JoyceTaxonomyURI extends JoyceURI {

	protected final String domain;
	protected final String product;

	protected JoyceTaxonomyURI(URI uri, String kind, String name, String domain, String product) {
		super(uri, kind, name);
		this.domain = domain;
		this.product = product;
	}

	public String getFullName() {
		return String.format("%s:%s:%s", domain, product, name);
	}
}
