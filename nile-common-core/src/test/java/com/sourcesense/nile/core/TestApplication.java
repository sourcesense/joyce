package com.sourcesense.nile.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.service.SchemaService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.sourcesense.nile", exclude={CassandraDataAutoConfiguration.class})
public class TestApplication {

	@Bean
	ObjectMapper mapper(){
		return new ObjectMapper();
	}

}
