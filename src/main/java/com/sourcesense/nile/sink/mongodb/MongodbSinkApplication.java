package com.sourcesense.nile.sink.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.sourcesense.nile"})
public class MongodbSinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongodbSinkApplication.class, args);
	}

}
