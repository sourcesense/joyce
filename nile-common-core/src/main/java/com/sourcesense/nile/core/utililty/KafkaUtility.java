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

package com.sourcesense.nile.core.utililty;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class KafkaUtility {
    public static void addTopicIfNeeded(KafkaAdmin kafkaAdmin, String topic, Integer partitions, Integer replicas, Integer retention, String cleanupPolicy) throws ExecutionException, InterruptedException {
        AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        List<NewTopic> topics = new ArrayList<>();
        topics.add(TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicas)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicy)
                .config(TopicConfig.RETENTION_MS_CONFIG, retention.toString())
                .build());
        try {
            adminClient.describeTopics(Collections.singletonList(topic)).all().get();
        } catch (ExecutionException e){
            // topic does not exists
            adminClient.createTopics(topics).all().get();
        }
    }

}
