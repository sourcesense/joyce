package com.sourcesense.joyce.core.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.common.TracingCommandListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "mongodb")
@Configuration
@EnableMongoRepositories(basePackages = "com.sourcesense.joyce.core.dao.mongodb")
@RequiredArgsConstructor
public class MongodbConfig  extends AbstractMongoClientConfiguration {

	private final ApplicationContext applicationContext;
	private final Tracer tracer;

	@Value("${joyce.data.mongodb.uri:mongodb://localhost:27017/joyce}")
	String mongoUri;

	@Value("${joyce.data.mongodb.database:joyce}")
	String database;

	@Override
	public MongoClient mongoClient() {
		// Instantiate TracingCommandListener
		TracingCommandListener listener = new TracingCommandListener.Builder(tracer).build();
		ConnectionString connectionString = new ConnectionString(mongoUri);
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.addCommandListener(listener)
				.applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Override
	public MongoMappingContext mongoMappingContext(MongoCustomConversions customConversions) throws ClassNotFoundException {
		MongoMappingContext context = super.mongoMappingContext(customConversions);
		context.setApplicationContext(applicationContext);
		return context;
	}

	@Override
	public MongoTemplate mongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
		converter.setMapKeyDotReplacement("_");
		return new MongoTemplate(databaseFactory, converter);
	}

	@Override
	protected String getDatabaseName() {
		return database;
	}

}
