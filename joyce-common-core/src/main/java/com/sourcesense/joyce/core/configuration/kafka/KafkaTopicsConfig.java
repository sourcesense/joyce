package com.sourcesense.joyce.core.configuration.kafka;

import com.sourcesense.joyce.core.configuration.kafka.topics.ContentTopicConfig;
import com.sourcesense.joyce.core.configuration.kafka.topics.ImportTopicConfig;
import com.sourcesense.joyce.core.configuration.kafka.topics.NotificationTopicConfig;
import lombok.RequiredArgsConstructor;
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

@ConditionalOnProperty(value = "joyce.kafka.bootstrapAddress")
@Configuration
@RequiredArgsConstructor
@EnableKafka
public class KafkaTopicsConfig {

	private final ContentTopicConfig contentTopicConfig;
	private final NotificationTopicConfig notificationTopicConfig;
	private final ImportTopicConfig importTopicConfig;


	@Value(value = "${joyce.kafka.bootstrapAddress}")
	private String bootstrapAddress;

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		return new KafkaAdmin(configs);
	}

	@Bean
	public NewTopic contentTopic() {
		return TopicBuilder.name(contentTopicConfig.getTopic())
				.partitions(contentTopicConfig.getPartitions())
				.replicas(contentTopicConfig.getReplicas())
				.config(TopicConfig.RETENTION_MS_CONFIG, contentTopicConfig.getRetention().toString())
				.config(TopicConfig.CLEANUP_POLICY_CONFIG, contentTopicConfig.getCleanup())
				.build();
	}

	@Bean
	public NewTopic importTopic() {
		return TopicBuilder.name(importTopicConfig.getTopic())
				.partitions(importTopicConfig.getPartitions())
				.replicas(importTopicConfig.getReplicas())
				.config(TopicConfig.RETENTION_MS_CONFIG, importTopicConfig.getRetention().toString())
				.config(TopicConfig.CLEANUP_POLICY_CONFIG, importTopicConfig.getCleanup())
				.build();
	}
	@Bean
	public NewTopic notificationTopic() {
		return TopicBuilder.name(notificationTopicConfig.getTopic())
				.partitions(notificationTopicConfig.getPartitions())
				.replicas(notificationTopicConfig.getReplicas())
				.config(TopicConfig.RETENTION_MS_CONFIG, notificationTopicConfig.getRetention().toString())
				.config(TopicConfig.CLEANUP_POLICY_CONFIG, notificationTopicConfig.getCleanup())
				.build();
	}
}
