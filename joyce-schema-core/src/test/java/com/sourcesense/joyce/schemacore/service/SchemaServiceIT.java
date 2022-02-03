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

package com.sourcesense.joyce.schemacore.service;

import com.sourcesense.joyce.schemacore.TestApplication;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class SchemaServiceIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();


    @Test
    void schemaServiceShouldBeAutoConfigured(){
        this.contextRunner.withPropertyValues("joyce.data.mongodb.enabled=true")
                .withBean(SchemaService.class, () -> Mockito.mock(SchemaService.class))
                .withUserConfiguration(TestApplication.class)
                .run(context -> Assertions.assertThat(context).hasBean("schemaService"));
    }

    @Test
    void schemaServiceShouldNotBeAutoConfigured(){
        this.contextRunner.withPropertyValues("joyce.data.mongodb.enabled=false")
                .withBean(SchemaService.class, () -> Mockito.mock(SchemaService.class))
                .withUserConfiguration(TestApplication.class)
                .run(context -> Assertions.assertThat(context).doesNotHaveBean("schemaService"));
    }
}
