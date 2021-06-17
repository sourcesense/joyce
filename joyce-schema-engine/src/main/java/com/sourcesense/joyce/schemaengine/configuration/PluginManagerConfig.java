package com.sourcesense.joyce.schemaengine.configuration;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class PluginManagerConfig {

	@Value("${joyce.schema-engine.plugin-path:/app/custom-handlers}")
	private String jarPath;

	@Bean
	SpringPluginManager pluginManager() {
		return new SpringPluginManager(Paths.get(jarPath));
	}
}
