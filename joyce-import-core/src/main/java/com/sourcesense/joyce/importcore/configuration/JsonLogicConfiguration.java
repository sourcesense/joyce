package com.sourcesense.joyce.importcore.configuration;

import io.github.jamsesso.jsonlogic.JsonLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonLogicConfiguration {

	@Bean
	JsonLogic jsonLogic() {
		return new JsonLogic();
	}
}
