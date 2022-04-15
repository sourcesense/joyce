package com.sourcesense.joyce.core.mapping.mongo.reading;

import com.sourcesense.joyce.core.model.uri.JoyceDocumentURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class JoyceDocumentURIReadingConverter implements Converter<String, JoyceDocumentURI> {

	private final JoyceURIFactory joyceURIFactory;

	@Override
	public JoyceDocumentURI convert(@NonNull String stringURI) {
		return joyceURIFactory.createURIOrElseThrow(stringURI, JoyceDocumentURI.class);
	}
}
