package com.sourcesense.nile.schemaengine;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.nile.schemaengine.handlers.TransormerHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.sourcesense.nile")
public class TestApplication {

	@Bean(name = "fooHandler")
	TransormerHandler handler(){
		TransormerHandler transormerHandler = (key, schema, sourceJsonNode) -> {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode returnNode = mapper.createObjectNode();
			returnNode.set(key, new TextNode("bar"));
			return  returnNode;
		};
		return transormerHandler;
	}
}
