/*
 * Copyright 2021 Sourcesense Spa
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

package com.sourcesense.nile.core.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.configuration.KsqlDBConfig;
import com.sourcesense.nile.core.configuration.SchemaServiceProperties;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.model.SchemaEntity;
import io.confluent.ksql.api.client.BatchedQueryResult;
import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.KsqlObject;
import io.confluent.ksql.api.client.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "nile.schema-service.database", havingValue = "kafka")
@Component
public class KafkaSchemaDao implements SchemaDao {

    private final Client ksql;
    private final SchemaServiceProperties schemaServiceProperties;
    private final ObjectMapper mapper;

    private static final String TABLE_NAME = "NILE_SCHEMA_TABLE";
    private static final String STREAM_NAME = "NILE_SCHEMA_STREAM";


    @PostConstruct
    void init() throws ExecutionException, InterruptedException {
        String createTable = String.format(
                "CREATE TABLE IF NOT EXISTS %s (\n" +
                "     uid VARCHAR PRIMARY KEY,\n" +
                "     value VARCHAR\n," +
                "     subtype VARCHAR\n" +
                "   ) WITH (\n" +
                "     KAFKA_TOPIC = '%s', \n" +
                "     VALUE_FORMAT = 'JSON'\n" +
                "   );", TABLE_NAME, KsqlDBConfig.SCHEMA_TOPIC);
        ksql.executeStatement(createTable).get();
        String createStream = String.format(
                "CREATE STREAM IF NOT EXISTS %s (\n" +
                        "     uid VARCHAR KEY,\n" +
                        "     value VARCHAR\n," +
                        "     subtype VARCHAR\n" +
                        "   ) WITH (\n" +
                        "     KAFKA_TOPIC = '%s', \n" +
                        "     VALUE_FORMAT = 'JSON'\n" +
                        "   );", STREAM_NAME, KsqlDBConfig.SCHEMA_TOPIC);
        ksql.executeStatement(createStream).get();
        String createMaterializedView = String.format(
            "CREATE TABLE %s AS SELECT * FROM %s WHERE subtype = '%s';", getSchemaTableName(), TABLE_NAME, schemaServiceProperties.getSubtype());
        ksql.executeStatement(createMaterializedView).get();
    }

    private String getSchemaTableName() {
        return String.format("%s_%s", TABLE_NAME, schemaServiceProperties.getSubtype().toUpperCase());
    }

    @Override
    public Optional<SchemaEntity> get(String id) {
        String query = String.format("SELECT value FROM %s WHERE uid = '%s';", getSchemaTableName(), id);
        BatchedQueryResult result = ksql.executeQuery(query);
        // Wait for query result
        try {
            List<Row> resultRows = result.get();
            if (resultRows.size() > 0){
                Row res = resultRows.get(0);
                String schemaString = res.getString("VALUE");
                SchemaSave schema = mapper.readValue(schemaString, SchemaSave.class);
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            log.error("Cannot retrieve schema {} error: {}", id, e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public List<SchemaEntity> getAll() {
        String query = String.format("SELECT value FROM %s ;", getSchemaTableName());
        try {
            List<Row> result = ksql.executeQuery(query).get();
            return result.stream()
                    .map(row -> row.getString("VALUE"))
                    .map(s -> mapper.readValue(s, SchemaSave.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        }


    }

    @Override
    public void save(SchemaEntity schemaEntity) {
        KsqlObject row = new KsqlObject()
                .put("uid", schemaEntity.getUid())
                .put("subtype", schemaServiceProperties.getSubtype())
                .put("value", schemaEntity.getSchema());

        try {
            ksql.insertInto(STREAM_NAME, row).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void delete(SchemaEntity t) {

    }
}
