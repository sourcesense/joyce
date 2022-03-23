package com.sourcesense.joyce.core.enumeration.uri;

import lombok.Getter;

import java.util.List;

public class JoyceURIKind {

	public final static String CONTENT = "content";
	public final static String API = "api";
	public final static String SECRET = "secret";

	@Getter
	protected final static List<String> values = List.of(CONTENT, API, SECRET);

	public static boolean isValid(String kind) {
		return values.contains(kind);
	}
}
