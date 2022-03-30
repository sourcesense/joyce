package com.sourcesense.joyce.core.enumeration.uri;

import lombok.Getter;

import java.util.List;

public class JoyceURIRestOrigin {

	public final static String SINGLE = "single";
	public final static String BULK = "bulk";

	@Getter
	protected final static List<String> values = List.of(SINGLE, BULK);

	public static boolean isValid(String origin) {
		return values.contains(origin);
	}

}
