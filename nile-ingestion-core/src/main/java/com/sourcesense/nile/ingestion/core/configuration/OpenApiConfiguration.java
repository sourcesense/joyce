package com.sourcesense.nile.ingestion.core.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
		info = @Info(
				title = "Nile Ingestion Api",
				description = "Api to ingest documents through Nile, and to manage associated schemas",
				version = "0.1.0",
				contact = @Contact(
						url = "https://nile.sourcesense.com",
						email = "nile@sourcesense.com")
		),
		servers = @Server(url = "http://localhost:8080/api"))
@Configuration
public class OpenApiConfiguration {
}
