package com.sourcesense.joyce.core.mapping.mongo.reading;

import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class JoyceSchemaURIReadingConverter implements Converter<String, JoyceSchemaURI> {

	private final JoyceURIFactory joyceURIFactory;

	@Override
	public JoyceSchemaURI convert(@NonNull String stringURI) {
		return joyceURIFactory.createURIOrElseThrow(stringURI, JoyceSchemaURI.class);
	}
}
