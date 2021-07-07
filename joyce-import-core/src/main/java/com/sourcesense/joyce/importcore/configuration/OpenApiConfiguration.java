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

package com.sourcesense.joyce.importcore.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
		info = @Info(
				title = "Joyce Import Api",
				description = "Api to import documents through Joyce, and to manage associated schemas",
				version = "0.1.0",
				contact = @Contact(
						url = "https://joyce.sourcesense.com",
						email = "joyce@sourcesense.com")
		),
		servers = {@Server(url = "http://localhost:6651"), @Server(url = "http://import-gateway.joyce.oc.corp.sourcesense.com")})
@Configuration
public class OpenApiConfiguration {
}
