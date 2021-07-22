package com.sourcesense.joyce.core.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum FileExtension {

	CSV(".csv"),
	XLS(".xls"),
	XLSX(".xlsx"),
	UNDEFINED("");

	private final String extension;

	public static FileExtension getFileExtensionFromName(String fileName) {
		return Arrays.stream(values())
				.filter(value -> fileName.endsWith(value.extension))
				.findFirst()
				.orElse(UNDEFINED);
	}
}
