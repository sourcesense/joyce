package com.sourcesense.nile.ingestion.core.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("mongodb")
@EnableAutoConfiguration(exclude={CassandraDataAutoConfiguration.class})
public class MongodbDaoTest {

	@Autowired
	Dao<SchemaEntity> schemaEntityDao;

	@Test
	void schemaDaoShouldBe(){
		Assertions.assertTrue(schemaEntityDao.getClass().equals(MongodbSchemaDao.class));

		Assertions.assertEquals(Optional.empty(), schemaEntityDao.get("asd"));
	}

	@Test
	void addRemove(){
		ObjectMapper mapper = new ObjectMapper();
		SchemaEntity schemaEntity = new SchemaEntity();
		schemaEntity.setUid("user-schema");
		ObjectNode node = mapper.createObjectNode();
		node.put("foo", "bar");
		node.put("count", 4);
		node.set("object", mapper.createObjectNode().put("miao", "bau"));
		Map schema =   mapper.convertValue(node, Map.class);
		schemaEntity.setSchema(schema);
		Assertions.assertDoesNotThrow(() -> {
			schemaEntityDao.save(schemaEntity);
		});

		List<SchemaEntity> find1 = schemaEntityDao.getAll();
		Assertions.assertTrue(find1.size() > 0);

		Assertions.assertDoesNotThrow(() -> {
			schemaEntityDao.delete(schemaEntity);
		});

		List<SchemaEntity> find2 = schemaEntityDao.getAll();
		Assertions.assertTrue(find2.size() == 0);

	}
}
