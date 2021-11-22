/*
 * Copyright 2021 Sourcesense Spa
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

package com.sourcesense.joyce.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("schemaServiceProperties")
@ConfigurationProperties("joyce.schema-service")
public class SchemaServiceProperties {
	/**
	 * Enable the service or not
 	 */
	Boolean enabled = false;
	/**
	 * Database that bakes the CRUD of Schema, could be: mongodb | kafka | rest
	 */
	String database = "mongodb";

	/**
	 * Name of the collection topic that bakes scehmas
	 */
	String collection = "joyce_schema";

	/**
	 * Used by the Rest implementation, it is the endpoint
	 */
	String restEndpoint;

	/**
	 * Used by the Rest implementation, if present it is used as Basic Auth credential to authenticate to the
	 */
	String restCredentials;

	/**
	 * Subtype of the managed schema
	 * DEPRECATED: used only by kafka cbacking mechanism
	 */
	@Deprecated
	String subtype = "import";

	/**
	 * Used for kafka topics creation
	 */
	@Deprecated
	Integer partitions = 10;

	/**
	 * Used for kafka topics creation
	 */
	@Deprecated
	Integer replicas = 1;

	/**
	 * Used for kafka topics creation
	 */
	@Deprecated
	private Integer retention = 259200000; // 3 days
}
