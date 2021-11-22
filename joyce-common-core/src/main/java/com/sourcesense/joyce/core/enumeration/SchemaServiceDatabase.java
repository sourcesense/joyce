package com.sourcesense.joyce.core.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum SchemaServiceDatabase {

	KAFKA("kafka"),
	MONGODB("mongodb"),
	REST("rest"),
	UNDEFINED("");

	private final String value;

	private final static Map<String, SchemaServiceDatabase> databaseSelector;

	static {
		databaseSelector = Arrays.stream(values())
			.collect(Collectors.toMap(
					SchemaServiceDatabase::getValue,
					Function.identity()
			));
	}

	public static SchemaServiceDatabase getDatabaseFromValue(String value) {
		return databaseSelector.getOrDefault(value, UNDEFINED);
	}
}
