package com.sourcesense.joyce.core.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ObjectToStringDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(
			JsonParser jsonParser,
			DeserializationContext deserializationContext) throws IOException {

		ObjectCodec codec = jsonParser.getCodec();
		return codec.readTree(jsonParser).toString();
	}
}
