package com.sourcesense.joyce.core.mapping.mongo.reading;

import com.sourcesense.joyce.core.model.uri.JoyceTaxonomyURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class JoyceTaxonomyURIReadingConverter implements Converter<String, JoyceTaxonomyURI> {

	private final JoyceURIFactory joyceURIFactory;

	@Override
	public JoyceTaxonomyURI convert(@NonNull String stringURI) {
		return joyceURIFactory.createURIOrElseThrow(stringURI, JoyceTaxonomyURI.class);
	}
}
