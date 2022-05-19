package com.sourcesense.joyce.core.configuration.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.sourcesense.joyce.core.mapping.mongo.JoyceURIMongoConverters;
import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.common.TracingCommandListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@RequiredArgsConstructor
public abstract class AbstractMongodbConfiguration extends AbstractMongoClientConfiguration {

	protected final String database;
	protected final String mongoUri;

	protected final Tracer tracer;
	protected final ApplicationContext applicationContext;
	protected final JoyceURIMongoConverters joyceURIMongoConverters;

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
	protected void configureConverters(MongoCustomConversions.MongoConverterConfigurationAdapter converterConfigurationAdapter) {
		joyceURIMongoConverters.getConverters().forEach(converterConfigurationAdapter::registerConverter);
	}

	@Override
	protected String getDatabaseName() {
		return database;
	}

	protected MongoTemplate computeMongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
		converter.setMapKeyDotReplacement("_");
		return new MongoTemplate(databaseFactory, converter);
	}
}
