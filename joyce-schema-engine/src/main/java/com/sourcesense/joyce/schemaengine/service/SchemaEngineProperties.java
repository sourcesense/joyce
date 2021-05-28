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

package com.sourcesense.joyce.schemaengine.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("joyce.schema-engine")
@Getter @Setter
@Component
public class SchemaEngineProperties {
	/**
	 * Map of handlers to load
	 * the key is the key that you want to implement in the json-schema
	 * tha value is a Bean name that you provide as an injectable Component
	 */
	private Map<String, String> handlers = new HashMap<>();
}
