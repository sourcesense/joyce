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
    Boolean enabled = false;
    String database = "kafka";
    String subtype = "import";
    String topic = "joyce_schema";
    Integer partitions = 10;
    Integer replicas = 1;
		/**
		 * Notification topic retention in milliseconds
		 */
		private Integer retention = 259200000; // 3 days

    public String getCollection(){
    	return String.format("joyce_schema_%s", subtype);
		}
}
