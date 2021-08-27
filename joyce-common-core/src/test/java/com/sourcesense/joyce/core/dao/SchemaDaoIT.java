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

package com.sourcesense.joyce.core.dao;


import com.sourcesense.joyce.core.TestApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;


@SpringBootTest
public class SchemaDaoIT {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

	@Test
	void schemaDaoShouldBeKafkaWhenPropertySchemaServiceEnabled(){
		this.contextRunner
				.withUserConfiguration(TestApplication.class)
				.withPropertyValues("joyce.schema-service.enabled=true","joyce.notification-service.enabled=true")
				.run(context -> {
					SchemaDao dao = context.getBean(SchemaDao.class);
					Assertions.assertTrue(dao instanceof KafkaSchemaDao);
				});
	}


}
