package com.sourcesense.joyce.schemaengine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	RestTemplate defaultRestTemplate() {
		return new RestTemplate();
	}
}
