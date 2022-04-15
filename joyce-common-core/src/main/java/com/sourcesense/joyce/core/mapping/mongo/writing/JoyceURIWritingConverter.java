package com.sourcesense.joyce.core.mapping.mongo.writing;

import com.sourcesense.joyce.core.model.uri.JoyceURI;
import org.springframework.core.convert.converter.Converter;

public class JoyceURIWritingConverter implements Converter<JoyceURI, String> {

	@Override
	public String convert(JoyceURI joyceURI) {
		return joyceURI.toString();
	}
}
