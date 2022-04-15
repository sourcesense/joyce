package com.sourcesense.joyce.core.mapping.mongo;

import lombok.Getter;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.List;

@Getter
public class JoyceURIMongoConverters {

	protected final List<Converter<?, ?>> converters;

	protected JoyceURIMongoConverters(Converter<?, ?>... converters) {
		this.converters = Arrays.asList(converters);
	}

	protected JoyceURIMongoConverters(List<Converter<?, ?>> converters) {
		this.converters = converters;
	}

	public static JoyceURIMongoConverters from(List<Converter<?, ?>> converters) {
		return new JoyceURIMongoConverters(converters);
	}

	public static JoyceURIMongoConverters of(Converter<?, ?>... converters) {
		return new JoyceURIMongoConverters(converters);
	}
}
