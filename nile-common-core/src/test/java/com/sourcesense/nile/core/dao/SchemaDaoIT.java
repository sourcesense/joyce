package com.sourcesense.nile.core.dao;


import com.sourcesense.nile.core.TestApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@SpringBootTest
@EnableAutoConfiguration(exclude={CassandraDataAutoConfiguration.class})
public class SchemaDaoIT {
	private final ApplicationContextRunner contextRunner
			= new ApplicationContextRunner();

	@Test
	void schemaDaoShouldBeCassandraWhenPropertyDatabaseSet(){
		this.contextRunner.withPropertyValues("nile.schema-service.database=cassandra")
				.withUserConfiguration(TestApplication.class)
				.run(context -> {
					SchemaDao dao = context.getBean(SchemaDao.class);
					Assertions.assertTrue(dao instanceof CassandraSchemaDao);
				});
	}


	@Test
	void schemaDaoShouldBeMongodbWhenPropertyDatabaseSet(){
		this.contextRunner.withPropertyValues("nile.schema-service.database=mongodb")
				.withUserConfiguration(TestApplication.class)
				.run(context -> {
					SchemaDao dao = context.getBean(SchemaDao.class);
					Assertions.assertTrue(dao instanceof MongodbSchemaDao);
				});
	}


}

