package com.sourcesense.joyce.schemaengine.configuration;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	RestTemplate defaultRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	MustacheFactory mustacheFactory(){
		return new DefaultMustacheFactory();
	};
}
