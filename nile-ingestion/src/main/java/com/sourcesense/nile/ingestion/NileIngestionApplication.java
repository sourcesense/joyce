package com.sourcesense.nile.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sourcesense.nile")
public class NileIngestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(NileIngestionApplication.class, args);
	}

}
