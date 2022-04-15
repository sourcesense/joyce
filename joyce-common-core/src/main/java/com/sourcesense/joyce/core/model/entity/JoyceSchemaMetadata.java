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

package com.sourcesense.joyce.core.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class JoyceSchemaMetadata {

	@JsonProperty("uid")
	private String uidKey;
	private String type;
	private String domain;
	private String product;
	private String name;
	private String collection;
	private String description;

	private JoyceSchemaURI parent;
	private List<Map<String, Object>> indexes;

	private Boolean development = false;
	private Boolean store = true;
	private Boolean validation = true;
	private Boolean indexed = true;
	private Boolean connectors = false;
	private Boolean export = false;

	private Map<String, Object> extra;

	/**
	 * Validate the object, some keys are required or not given a subtype
	 * throws if it fails to validate
	 *
	 * @return
	 * @throws InvalidMetadataException
	 */
	public JoyceSchemaMetadata validate() throws InvalidMetadataException {
		if (name == null) {
			throw new InvalidMetadataException("Missing [name] from metadata");
		}

		if (type == null) {
			throw new InvalidMetadataException("Missing [subtype] from metadata");
		}

		if (parent != null) {
			return this;
		}

		if (uidKey == null) {
			throw new InvalidMetadataException("Missing [uid] from metadata");
		}

		if (collection == null) {
			throw new InvalidMetadataException("Missing [collection] from metadata");
		}
		return this;
	}
}
