package com.sourcesense.joyce.schemacore.model.entity;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadata;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "#{@mongodbProperties.getSchemaCollection()}")
@Getter @Setter
public class SchemaDocument {

	@Id
	private String uid;
	private String schema;
	private SchemaMetadata metadata;
	private String type;
	private List<String> required;
	private String properties;
}
