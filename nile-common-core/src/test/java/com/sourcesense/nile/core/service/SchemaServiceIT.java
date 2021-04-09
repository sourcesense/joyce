package com.sourcesense.nile.core.service;

import com.sourcesense.nile.core.TestApplication;
import com.sourcesense.nile.core.dao.MongodbSchemaDao;
import com.sourcesense.nile.core.dao.SchemaDao;
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
        this.contextRunner.withPropertyValues("nile.schema-service.enabled=true")
                .withBean(SchemaDao.class, () -> Mockito.mock(SchemaDao.class))
                .withUserConfiguration(TestApplication.class)
                .run(context -> {
                    Assertions.assertThat(context).hasBean("schemaService");
                });
    }

    @Test
    void schemaServiceShouldNOTbeAutoconfigured(){
        this.contextRunner.withPropertyValues("nile.schema-service.enabled=false")
                .withBean(SchemaDao.class, () -> Mockito.mock(SchemaDao.class))
                .withUserConfiguration(TestApplication.class)
                .run(context -> {
                    Assertions.assertThat(context).doesNotHaveBean("schemaService");
                });
    }
}
