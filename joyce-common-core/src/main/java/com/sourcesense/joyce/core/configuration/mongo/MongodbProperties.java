package com.sourcesense.joyce.core.configuration.mongo;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * This class is used to map metadata mongo indexes from configuration yaml
 */
@Data
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "joyce.data.mongodb")
public class MongodbProperties {

	/**
	 * Enable or disable mongo
	 */
	private Boolean enabled = false;

	/**
	 * Name of the collection topic that bakes schemas
	 */
	private String schemaCollection = "joyce_schema";

	/**
	 * Used for metadata indexes creation
	 */
	private List<Map<String, Object>> metadataIndexes;
}
