package com.sourcesense.joyce.core.model.uri;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sourcesense.joyce.core.mapping.deserializer.JoyceURIDeserializer;
import com.sourcesense.joyce.core.mapping.serializer.JoyceURISerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;

@Getter
@EqualsAndHashCode
@JsonDeserialize(using = JoyceURIDeserializer.class)
@JsonSerialize(using = JoyceURISerializer.class)
public class JoyceURI {

	public static final String URI_SCHEMA = "joyce";

	protected final URI uri;
	protected final String kind;
	protected final String name;

	protected JoyceURI(URI uri, String kind, String name) {
		this.uri = uri;
		this.kind = kind;
		this.name = name;
	}

	@Override
	public String toString() {
		return uri.toString();
	}

}
