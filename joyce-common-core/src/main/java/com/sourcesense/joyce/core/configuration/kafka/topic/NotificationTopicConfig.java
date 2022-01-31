package com.sourcesense.joyce.core.configuration.kafka.topic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("joyce.kafka.notification")
@Component
public class NotificationTopicConfig extends KafkaTopic {
}
