/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.dao.SchemaDao;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.mapper.SchemaMapper;
import com.sourcesense.nile.core.model.NileURI;
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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemaServiceTest {
	@Mock
	SchemaDao schemaEntityDao;

	@Mock
	SchemaMapper schemaMapper;

	@Mock
	SchemaEngine schemaEngine;

	protected Path loadResource(String name) throws URISyntaxException {
		URL res =  this.getClass().getClassLoader().getResource(name);
		return Paths.get(res.toURI());
	}

//    @Test
// TODO: implement this test logic changed
    void breakingChangeSchemaShouldStepVersion() throws Exception {
		String schemaRaw = Files.readString(loadResource("schema.json"));
		ObjectMapper mapper = new ObjectMapper();

		SchemaSave schema = mapper.readValue(schemaRaw, SchemaSave.class);
		SchemaService service = new SchemaService(schemaEntityDao, schemaMapper, schemaEngine);

		Mockito.when(schemaEngine.hasBreakingChanges(any(), any())).thenReturn(true);

//		Mockito.when(schemaMapper.toEntity(schema)).thenReturn(entityNew);
		SchemaEntity entity = mock(SchemaEntity.class);

		Mockito.when(schemaEntityDao.get("nile://schema/import/foobar"))
				.thenReturn(Optional.of(entity));


		NileURI ret = service.save(schema);

		Assertions.assertEquals("nile://schema/import/foobar", ret.toString());

    }
}
