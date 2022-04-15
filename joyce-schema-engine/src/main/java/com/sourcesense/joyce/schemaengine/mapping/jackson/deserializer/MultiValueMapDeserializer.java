package com.sourcesense.joyce.schemaengine.mapping.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MultiValueMapDeserializer extends JsonDeserializer<MultiValueMap<String, String>> {

	@Override
	public MultiValueMap<String, String> deserialize(
			JsonParser jsonParser,
			DeserializationContext deserializationContext) throws IOException {

		ObjectCodec codec = jsonParser.getCodec();
		Map<String, String> map = codec.readValue(jsonParser, new TypeReference<>() {});

		return new LinkedMultiValueMap<>(map.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						this::computeValuesForKey
				)));
	}

	private List<String> computeValuesForKey(Map.Entry<String, String> entry) {
		return Optional.of(entry)
				.map(Map.Entry::getValue)
				.map(value -> value.split(",")).stream()
				.flatMap(Arrays::stream)
				.map(String::trim)
				.collect(Collectors.toList());
	}
}
