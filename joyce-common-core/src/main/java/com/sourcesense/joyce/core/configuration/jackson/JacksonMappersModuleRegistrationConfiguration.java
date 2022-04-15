package com.sourcesense.joyce.core.configuration.jackson;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class JacksonMappersModuleRegistrationConfiguration {

	private final AbstractJacksonMappersModuleRegisterer jacksonMappersModuleRegisterer;

	@PostConstruct
	public void registerModules() {
		jacksonMappersModuleRegisterer.registerJoyceURIModule();
	}
}
