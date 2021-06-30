package com.sourcesense.joyce.sink.mongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@ConfigurationProperties(prefix = "joyce.data.mongodb")
public class MetadataIndexesProperties {

	private List<Map<String, Object>> metadataIndexes;
}
