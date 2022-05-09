package com.sourcesense.joyce.core.configuration.mongo;

import com.sourcesense.joyce.core.mapping.mongo.reading.*;
import com.sourcesense.joyce.core.mapping.mongo.writing.JoyceURIWritingConverter;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractMongodbCustomConvertersConfig {

	protected final JoyceURIFactory joyceURIFactory;

	protected List<Converter<?, ?>> computeDefaultJoyceURIMongoConverters() {
		return List.of(
				new JoyceURIWritingConverter(),
				new JoyceURIReadingConverter(joyceURIFactory),
				new JoyceTaxonomyURIReadingConverter(joyceURIFactory),
				new JoyceSchemaURIReadingConverter(joyceURIFactory),
				new JoyceContentURIReadingConverter(joyceURIFactory),
				new JoyceSourceURIReadingConverter(joyceURIFactory),
				new JoyceDocumentURIReadingConverter(joyceURIFactory)
		);
	}
}
