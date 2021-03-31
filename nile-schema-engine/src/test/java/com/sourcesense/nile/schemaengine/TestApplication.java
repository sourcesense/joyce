package com.sourcesense.nile.schemaengine;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.nile.schemaengine.handlers.TransormerHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.sourcesense.nile")
public class TestApplication {
	@Bean
	ObjectMapper mapper(){
		return new ObjectMapper();
	}

	@Bean(name = "fooHandler")
	TransormerHandler handler(){
		TransormerHandler transormerHandler = (schema, sourceJsonNode,context) -> new TextNode("bar");
		return transormerHandler;
	}
}
