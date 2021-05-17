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

package com.sourcesense.nile.core.configuration;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(value = "nile.schema-service.enabled", havingValue = "true")
@Configuration
@EnableKafka
public class KsqlDBConfig {
    public static String SCHEMA_TOPIC = "nile-schema";

    @Value("${nile.ksql.host:localhost}")
    String host;

    @Value("${nile.ksql.port:8088}")
    int port;


    @Value("${nile.kafka.bootstrapAddress}")
    String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean(value = "schemaTopic")
    public NewTopic topicBackingSchemasTable() {
        return TopicBuilder.name(SCHEMA_TOPIC)
                .partitions(6)
                .replicas(1) //TODO: externalize in configs
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
                .build();
    }

    @Bean(destroyMethod = "close")
    public Client intializeClient() {
        ClientOptions options = ClientOptions.create()
                .setHost(host)
                .setPort(port);
        Client client = Client.create(options);

        return client;
    }

}
