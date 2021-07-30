package com.sourcesense.joyce.schemaengine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfiguration {
	@Bean
	RestTemplate restTemplate(){
		return new RestTemplate();
	}
}
