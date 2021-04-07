package com.sourcesense.nile.mongodbprojector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.sourcesense.nile.mongodbprojector", "com.sourcesense.nile.core.configuration"})
public class MongodbProjectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongodbProjectorApplication.class, args);
	}

}
