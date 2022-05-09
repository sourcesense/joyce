package com.sourcesense.joyce.core.configuration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JacksonMappersModuleRegistererInit {

	private final ObjectMapper jsonMapper;
	private final YAMLMapper yamlMapper;

	@Bean
	@ConditionalOnMissingBean(AbstractJacksonMappersModuleRegisterer.class)
	JacksonMappersModuleRegisterer jacksonMapperModuleRegisterer() {
		return new JacksonMappersModuleRegisterer(jsonMapper, yamlMapper);
	}
}
