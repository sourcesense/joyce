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
import com.sourcesense.nile.core.model.SchemaEntity;
import io.confluent.ksql.api.client.BatchedQueryResult;
import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.KsqlObject;
import io.confluent.ksql.api.client.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "nile.schema-service.database", havingValue = "kafka")
@Component
@DependsOn({"schemaTopic"})
public class KafkaSchemaDao implements SchemaDao {

    private final Client ksql;
    private final SchemaServiceProperties schemaServiceProperties;
    private final ObjectMapper mapper;

    private static final String TABLE_NAME = "NILE_SCHEMA_TABLE";
    private static final String STREAM_NAME = "NILE_SCHEMA_STREAM";

    private static Boolean initialized = false;


    void init() {
        try {
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
                    "CREATE TABLE IF NOT EXISTS  %s AS SELECT * FROM %s WHERE subtype = '%s';", getSchemaTableName(), TABLE_NAME, schemaServiceProperties.getSubtype());
            //TODO: see what they responds here https://github.com/confluentinc/ksql/issues/7503
            ksql.executeStatement(String.format("DROP TABLE IF EXISTS %s;", getSchemaTableName())).get();
            ksql.executeStatement(createMaterializedView).get();
            initialized = true;
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }

    private String getSchemaTableName() {
        return String.format("%s_%s", TABLE_NAME, schemaServiceProperties.getSubtype().toUpperCase());
    }

    @Override
    public Optional<SchemaEntity> get(String id) {
        if(!initialized){ //TODO: clever way to do it, as post construct doesn't work cause we could have not created the backing topic
            this.init();
        }
        String query = String.format("SELECT uid, value FROM %s WHERE uid = '%s';", getSchemaTableName(), id);
        BatchedQueryResult result = ksql.executeQuery(query);
        // Wait for query result
        try {
            List<Row> resultRows = result.get();
            if (resultRows.size() > 0){
                Row row = resultRows.get(0);
                SchemaEntity schema = mapper.readValue(row.getString("VALUE"), SchemaEntity.class);
                schema.setUid(row.getString("UID"));
                return Optional.of(schema);
            }
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            log.error("Cannot retrieve schema {} error: {}", id, e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public List<SchemaEntity> getAll() {
        if(!initialized){
            this.init();
        }
        String query = String.format("SELECT uid, value FROM %s ;", getSchemaTableName());
        try {
            List<Row> result = ksql.executeQuery(query).get();
            List<SchemaEntity> entities = new ArrayList<>();
            for (Row row : result){
                try {
                    SchemaEntity schema = mapper.readValue(row.getString("VALUE"), SchemaEntity.class);
                    schema.setUid(row.getString("UID"));
                    entities.add(schema);
                } catch (JsonProcessingException e) {
                    log.error("Wrong value {}", e.getMessage());
                }
            }
            return entities;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void save(SchemaEntity schemaEntity) {
        if(!initialized){
            this.init();
        }
        try {
            KsqlObject row = new KsqlObject()
                    .put("uid", schemaEntity.getUid())
                    .put("subtype", schemaServiceProperties.getSubtype())
                    .put("value", mapper.writeValueAsString(schemaEntity));
            ksql.insertInto(STREAM_NAME, row).get();
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void delete(SchemaEntity t) {
        if(!initialized){
            this.init();
        }
    }
}
