package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.core.dao.SchemaDao;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.mapper.SchemaMapper;
import com.sourcesense.nile.core.model.SchemaEntity;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemaServiceTest {
		@Mock
		SchemaDao schemaEntityDao;

		@Mock
		SchemaMapper schemaMapper;

		@Mock
		SchemaEngine schemaEngine;

    @Test
    void breakingChangeSchemaShouldStepVersion() throws JsonProcessingException {
			SchemaService service = new SchemaService(schemaEntityDao, schemaMapper, schemaEngine);
			service.uidPattern = "nile://ingestion/schema/%s";
			SchemaSave schemaNew = null;
			SchemaEntity entityNew = new SchemaEntity();
			entityNew.setName("foobar");
			entityNew.setSchema("foo");
			entityNew.setDevelopment(false);

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
			Assertions.assertEquals(2, ret.getVersion());

    }
}
