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

package com.sourcesense.joyce.core.service;

import com.sourcesense.joyce.core.dao.SchemaDao;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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

}
