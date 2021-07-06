package com.sourcesense.joyce.sink.mongodb.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adapts type field to Mongodb BsonType specs.
 */
@Component
public class TypeSerializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(
			JsonParser jsonParser,
			DeserializationContext deserializationContext) throws IOException {

		ObjectCodec codec = jsonParser.getCodec();
		String type = codec.readValue(jsonParser, String.class);

		switch (type) {
			case "integer":
				return "int";
			case "number":
				return "double";
			case "boolean":
				return "bool";
			default:
				return type;
		}
	}
}
