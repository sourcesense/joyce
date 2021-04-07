package com.sourcesense.nile.ingestion.core.service;

import com.sourcesense.nile.ingestion.core.dao.Dao;
import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaSave;
import com.sourcesense.nile.ingestion.core.mapper.SchemaMapper;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemaServiceTest {
		@Mock
		Dao<SchemaEntity> schemaEntityDao;

		@Mock
		SchemaMapper schemaMapper;

		@Mock
		SchemaEngine schemaEngine;

    @Test
    void breakingChangeSchemaShouldStepVersion() {
			SchemaService service = new SchemaService(schemaEntityDao, schemaMapper, schemaEngine);
			SchemaSave schemaNew = null;
			SchemaEntity entityNew = new SchemaEntity();
			entityNew.setName("foobar");
			entityNew.setSchema("foo");

			SchemaEntity entityOld = new SchemaEntity();
			entityOld.setSchema("bar");
			entityOld.setName("foobar");
			entityOld.setVersion(1);

			Mockito.when(schemaEngine.hasBreakingChanges("bar", "foo")).thenReturn(true);

			Mockito.when(schemaMapper.toEntity(schemaNew)).thenReturn(entityNew);

			Mockito.when(schemaEntityDao.get("nile://ingestion/schema/foobar"))
					.thenReturn(Optional.of(entityOld))
					.thenReturn(Optional.of(entityNew));

			Mockito.when(schemaMapper.toDto(entityNew)).then(invocationOnMock -> {
				Schema s = new Schema();
				s.setVersion(entityNew.getVersion());
				return s;
			});

//			Mockito.verify(schemaEntityDao).save(Mockito.argThat(entity -> {
//				assertEquals("nile://ingestion/schema/foobar", entity.getUid());
//				return true;
//			}));
//			Mockito.verify(schemaEntityDao).save(entityNew);

			Schema ret = service.save(schemaNew);
			assertEquals(2, ret.getVersion());

    }
}
