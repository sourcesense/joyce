package com.sourcesense.joyce.schemaengine.model.dto.handler;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sourcesense.joyce.schemaengine.mapping.jackson.deserializer.MultiValueMapDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestHandlerArgs {

	private String url;
	private HttpMethod method;
	private String body;

	@JsonDeserialize(using = MultiValueMapDeserializer.class)
	private MultiValueMap<String, String> headers;
}
