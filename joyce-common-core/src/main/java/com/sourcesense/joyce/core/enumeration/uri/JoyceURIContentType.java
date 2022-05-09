package com.sourcesense.joyce.core.enumeration.uri;

import lombok.Getter;

import java.util.List;

public class JoyceURIContentType {

	public final static String SCHEMA = "schema";
	public final static String DOCUMENT = "doc";
	public final static String SOURCE = "src";

	@Getter
	protected final static List<String> values = List.of(SCHEMA, DOCUMENT, SOURCE);

	public static boolean isValid(String contentType) {
		return values.contains(contentType);
	}
}
