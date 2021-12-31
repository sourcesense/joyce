package com.sourcesense.joyce.core.configuration.kafka;

import com.sourcesense.joyce.core.configuration.kafka.topics.CommandTopicConfig;
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
	private final CommandTopicConfig commandTopicConfig;


	@Value(value = "${joyce.kafka.bootstrapAddress}")
	private String bootstrapAddress;

	@Bean
	@ConditionalOnProperty(value = "joyce.kafka.bootstrapAddress")
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		return new KafkaAdmin(configs);
	}

	@Bean
	@ConditionalOnProperty(value = "joyce.kafka.content.topic")
	public NewTopic contentTopic() {
		return this.kafkaTopic(contentTopicConfig);
	}

	@Bean
	@ConditionalOnProperty(value = "joyce.kafka.import.topic")
	public NewTopic importTopic() {
		return this.kafkaTopic(importTopicConfig);
	}

	@Bean
	@ConditionalOnProperty(value = "joyce.kafka.notification.topic")
	public NewTopic notificationTopic() {
		return this.kafkaTopic(notificationTopicConfig);
	}

	@Bean
	@ConditionalOnProperty(value = "joyce.kafka.command.topic")
	public NewTopic commandTopic() {
		return this.kafkaTopic(commandTopicConfig);
	}

	private NewTopic kafkaTopic(KafkaTopic kafkaTopicConfig) {
		return TopicBuilder.name(kafkaTopicConfig.getTopic())
				.partitions(kafkaTopicConfig.getPartitions())
				.replicas(kafkaTopicConfig.getReplicas())
				.config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicConfig.getRetention().toString())
				.config(TopicConfig.CLEANUP_POLICY_CONFIG, kafkaTopicConfig.getCleanup())
				.build();
	}
}
