package com.sourcesense.joyce.core.configuration.mongo;

import com.sourcesense.joyce.core.mapping.mongo.JoyceURIMongoConverters;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.sourcesense.joyce")
@ConditionalOnProperty(value = "joyce.data.mongodb.enabled", havingValue = "true")
public class MongodbConfiguration extends AbstractMongodbConfiguration {

	public MongodbConfiguration(
			Tracer tracer,
			ApplicationContext applicationContext,
			JoyceURIMongoConverters joyceURIMongoConverters,
			@Value("${joyce.data.mongodb.database:joyce}")	String database,
			@Value("${joyce.data.mongodb.uri:mongodb://localhost:27017/joyce}")	String mongoUri) {

		super(database, mongoUri, tracer, applicationContext, joyceURIMongoConverters);
	}

	@Override
	public MongoTemplate mongoTemplate(
			@Qualifier("mongoDbFactory") MongoDatabaseFactory databaseFactory,
			@Qualifier("mappingMongoConverter") MappingMongoConverter converter) {

		return super.computeMongoTemplate(databaseFactory, converter);
	}

	@Override
	public MongoDatabaseFactory mongoDbFactory() {
		return super.mongoDbFactory();
	}

	@Override
	public MappingMongoConverter mappingMongoConverter(
			@Qualifier("mongoDbFactory")MongoDatabaseFactory databaseFactory,
			MongoCustomConversions customConversions,
			MongoMappingContext mappingContext) {

		return super.mappingMongoConverter(databaseFactory, customConversions, mappingContext);
	}
}
