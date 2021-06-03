package com.sourcesense.joyce.schemaengine.configuration;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
@ConditionalOnProperty(value = "joyce.schema-transformer-plugin.enabled", havingValue = "true")
public class PluginManagerConfiguration {

	@Value("${joyce.schema-transformer-plugin.jar-path:/app/custom-handlers}")
	private String jarPath;

	@Bean
	SpringPluginManager pluginManager() {
		return new SpringPluginManager(Paths.get(jarPath));
	}
}
