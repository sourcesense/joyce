package com.sourcesense.joyce.core.model.uri;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JoyceSourceURI extends JoyceContentURI{

	protected final String channel;
	protected final String origin;
	protected final String uid;

	protected JoyceSourceURI(URI uri, String kind, String name, String domain, String product, String contentType, String channel, String origin, String uid) {
		super(uri, kind, name, domain, product, contentType);
		this.channel = channel;
		this.origin = origin;
		this.uid = uid;
	}
}
