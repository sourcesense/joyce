package com.sourcesense.joyce.core.mapping.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.sourcesense.joyce.core.mapping.mongo.reading.*;
import com.sourcesense.joyce.core.mapping.mongo.writing.JoyceURIWritingConverter;
import com.sourcesense.joyce.core.model.uri.JoyceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.core.test.JoyceURIWrapper;
import com.sourcesense.joyce.core.test.TestUtility;
import com.sourcesense.joyce.core.test.WithMongoTestBase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.List;
import java.util.Optional;

import static com.sourcesense.joyce.core.test.JoyceURIValues.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MongoCustomConvertersTest extends WithMongoTestBase implements TestUtility {

	private final static String MONGO_CONNECTION_STRING = "mongodb://localhost/joyce";
	private final static String MONGO_COLLECTION = "joyceURI";

	private MongoTemplate mongoTemplate;

	@BeforeEach
	public void init() {
		mongoTemplate = this.buildMongoTemplate();
	}

	@Test
	public void shouldConvertJoyceTaxonomyURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_TAXONOMY_URI);
	}

	@Test
	public void shouldConvertJoyceSchemaURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_SCHEMA_URI);
	}

	@Test
	public void shouldConvertJoyceSourceURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_SOURCE_URI);
	}

	@Test
	public void shouldConvertJoyceDocumentURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_DOCUMENT_URI);
	}

	private <J extends JoyceURI> void testJoyceURIToStringConversion(J joyceURI) throws JsonProcessingException {
		JoyceURIWrapper<J> document = new JoyceURIWrapper<>(666, joyceURI);
		mongoTemplate.save(document, MONGO_COLLECTION);
		assertEquals(
				jsonMapper.writeValueAsString(document),
				this.computeSavedDocument()
		);
	}

	private String computeSavedDocument() {
		return Optional.of(mongoTemplate)
				.map(template -> template.findById(666, Document.class, MONGO_COLLECTION))
				.map(Document::toJson)
				.orElse("");
	}

	private MongoTemplate buildMongoTemplate() {
		MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);

		MongoDatabaseFactory mongoDatabaseFactory = new SimpleMongoClientDatabaseFactory(mongoClient, "joyce");

		DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);

		List<Converter<?, ?>> mongoCustomConverters = this.computeMongoCustomConverters(JoyceURIFactory.getInstance());

		MongoCustomConversions mongoCustomConversions = MongoCustomConversions.create(
				converterConfigurationAdapter -> mongoCustomConverters.forEach(converterConfigurationAdapter::registerConverter)
		);

		MongoMappingContext mongoMappingContext = new MongoMappingContext();
		mongoMappingContext.setSimpleTypeHolder(mongoCustomConversions.getSimpleTypeHolder());

		MappingMongoConverter mappingMongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
		mappingMongoConverter.setMapKeyDotReplacement("_");

		return new MongoTemplate(mongoDatabaseFactory, mappingMongoConverter);
	}

	private List<Converter<?, ?>> computeMongoCustomConverters(JoyceURIFactory joyceURIFactory) {
		return List.of(
				new JoyceURIWritingConverter(),
				new JoyceURIReadingConverter(joyceURIFactory),
				new JoyceTaxonomyURIReadingConverter(joyceURIFactory),
				new JoyceSchemaURIReadingConverter(joyceURIFactory),
				new JoyceContentURIReadingConverter(joyceURIFactory),
				new JoyceSourceURIReadingConverter(joyceURIFactory),
				new JoyceDocumentURIReadingConverter(joyceURIFactory)
		);
	}
}
