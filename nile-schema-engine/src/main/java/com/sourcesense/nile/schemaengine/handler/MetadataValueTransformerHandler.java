/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.nile.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.schemaengine.annotation.SchemaTransformationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
@SchemaTransformationHandler(keyword = "$meta")
public class MetadataValueTransformerHandler extends JsonPathTransformerHandler {

	private final ObjectMapper mapper;

	@Override
	public JsonNode process(String key, JsonNode value, JsonNode source, Optional<JsonNode> metadata, Optional<Object> context) {
		JsonNode metadataAsSource = mapper.createObjectNode();
		if (metadata.isPresent()) {
			metadataAsSource = metadata.get();
		}
		return super.process(key, value, metadataAsSource, Optional.empty(), context);
	}
}
