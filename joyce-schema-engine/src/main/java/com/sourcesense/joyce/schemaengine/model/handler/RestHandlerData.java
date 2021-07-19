package com.sourcesense.joyce.schemaengine.model.handler;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.joyce.schemaengine.deserializer.MultiValueMapDeserializer;
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
public class RestHandlerData {

	private String host;
	private HttpMethod method;
	private String body;
	private TextNode extract;

	@JsonDeserialize(using = MultiValueMapDeserializer.class)
	private MultiValueMap<String, String> params;

	@JsonDeserialize(using = MultiValueMapDeserializer.class)
	private MultiValueMap<String, String> headers;
}
