package com.sourcesense.joyce.core.mapping.mongo.reading;

import com.sourcesense.joyce.core.model.uri.JoyceContentURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class JoyceContentURIReadingConverter implements Converter<String, JoyceContentURI> {

	private final JoyceURIFactory joyceURIFactory;

	@Override
	public JoyceContentURI convert(@NonNull String stringURI) {
		return joyceURIFactory.createURIOrElseThrow(stringURI, JoyceContentURI.class);
	}
}
