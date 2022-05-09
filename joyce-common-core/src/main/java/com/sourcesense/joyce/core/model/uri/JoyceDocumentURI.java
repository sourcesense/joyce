package com.sourcesense.joyce.core.model.uri;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JoyceDocumentURI extends JoyceContentURI{

	protected final String uid;

	protected JoyceDocumentURI(URI uri, String kind, String name, String domain, String product, String contentType, String uid) {
		super(uri, kind, name, domain, product, contentType);
		this.uid = uid;
	}
}
