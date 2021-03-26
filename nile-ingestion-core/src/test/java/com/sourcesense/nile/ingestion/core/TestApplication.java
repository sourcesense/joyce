package com.sourcesense.nile.ingestion.core;


import com.fasterxml.jackson.databind.ObjectMapper;
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
