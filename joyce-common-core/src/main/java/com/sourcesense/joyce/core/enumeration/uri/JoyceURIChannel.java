package com.sourcesense.joyce.core.enumeration.uri;

import lombok.Getter;

import java.util.List;

public class JoyceURIChannel {

	public final static String REST = "rest";
	public final static String CONNECT = "connect";

	@Getter
	protected final static List<String> values = List.of(REST, CONNECT);

	public static boolean isValid(String channel) {
		return values.contains(channel);
	}
}
