package com.sourcesense.nile.ingestion.core.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;


@Document
public class SchemaEntity {
	@Id
	private String uid;
	private Map schema;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Map getSchema() {
		return schema;
	}

	public void setSchema(Map schema) {
		this.schema = schema;
	}
}
