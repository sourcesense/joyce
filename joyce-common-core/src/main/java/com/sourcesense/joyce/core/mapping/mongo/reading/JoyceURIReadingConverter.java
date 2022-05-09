package com.sourcesense.joyce.core.mapping.mongo.reading;

import com.sourcesense.joyce.core.model.uri.JoyceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class JoyceURIReadingConverter implements Converter<String, JoyceURI> {

	private final JoyceURIFactory joyceURIFactory;

	@Override
	public JoyceURI convert(@NonNull String stringURI) {
		return joyceURIFactory.createURIOrElseThrow(stringURI);
	}
}
