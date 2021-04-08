package com.sourcesense.nile.core.dao;


import com.sourcesense.nile.core.model.SchemaEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("cassandra")
@EnableAutoConfiguration(exclude={CassandraDataAutoConfiguration.class})
public class CassandraDaoTestIT {

	@Autowired
	SchemaDao schemaEntityDao;

	@Test
	void schemaDaoShouldBe(){
		Assertions.assertTrue(schemaEntityDao.getClass().equals(CassandraSchemaDao.class));
		Assertions.assertEquals(Optional.empty(), schemaEntityDao.get("asd"));
	}



}

