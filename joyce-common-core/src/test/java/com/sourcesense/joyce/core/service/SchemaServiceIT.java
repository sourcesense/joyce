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

import com.sourcesense.joyce.core.TestApplication;
import com.sourcesense.joyce.core.dao.SchemaDao;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class SchemaServiceIT {
    private final ApplicationContextRunner contextRunner
            = new ApplicationContextRunner();


    @Test
    void schemaServiceShouldBeAutoconfigured(){
        this.contextRunner.withPropertyValues("joyce.schema-service.enabled=true")
                .withBean(SchemaDao.class, () -> Mockito.mock(SchemaDao.class))
                .withUserConfiguration(TestApplication.class)
                .run(context -> {
                    Assertions.assertThat(context).hasBean("schemaService");
                });
    }

    @Test
    void schemaServiceShouldNOTbeAutoconfigured(){
        this.contextRunner.withPropertyValues("joyce.schema-service.enabled=false")
                .withBean(SchemaDao.class, () -> Mockito.mock(SchemaDao.class))
                .withUserConfiguration(TestApplication.class)
                .run(context -> {
                    Assertions.assertThat(context).doesNotHaveBean("schemaService");
                });
    }
}
