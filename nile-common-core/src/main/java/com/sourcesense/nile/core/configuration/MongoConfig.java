package com.sourcesense.nile.core.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@ConditionalOnProperty(value = "nile.data.mongodb.uri")
@Configuration
public class MongoConfig  {
	@Value("${nile.data.mongodb.uri:mongodb://localhost:27017/ingestion}")
	String mongoUri;

	@Value("${nile.data.mongodb.database:ingestion}")
	String database;

	@Bean
	public MongoClient mongo() {
		ConnectionString connectionString = new ConnectionString(mongoUri);
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongo(), database);
	}
}
