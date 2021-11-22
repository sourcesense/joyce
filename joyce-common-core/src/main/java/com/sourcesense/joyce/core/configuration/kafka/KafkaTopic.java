package com.sourcesense.joyce.core.configuration.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaTopic {
	/**
	 * Notification topic
	 */
	private String topic;

	/**
	 * Notification topic partitions
	 */
	private Integer partitions = 10;

	/**
	 * Notification topic replicas
	 */
	private Integer replicas = 1;

	/**
	 * Notification topic retention in milliseconds
	 */
	private Integer retention = 259200000; // 3 days

	/**
	 * Topic cleanup policy
	 */
	private String cleanup =  "delete";
}
