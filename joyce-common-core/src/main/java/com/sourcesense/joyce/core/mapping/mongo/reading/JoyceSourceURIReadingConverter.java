package com.sourcesense.joyce.core.mapping.mongo.reading;

import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class JoyceSourceURIReadingConverter implements Converter<String, JoyceSourceURI> {

	private final JoyceURIFactory joyceURIFactory;

	@Override
	public JoyceSourceURI convert(@NonNull String stringURI) {
		return joyceURIFactory.createURIOrElseThrow(stringURI, JoyceSourceURI.class);
	}
}
