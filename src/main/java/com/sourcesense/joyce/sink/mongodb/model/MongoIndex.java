package com.sourcesense.joyce.sink.mongodb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MongoIndex {

	private String name;
	private Map<String, Object> fields;
}
