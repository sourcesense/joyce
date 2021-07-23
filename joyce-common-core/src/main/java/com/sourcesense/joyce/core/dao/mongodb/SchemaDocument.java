package com.sourcesense.joyce.core.dao.mongodb;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "#{@schemaServiceProperties.getCollection()}")
@Getter @Setter
public class SchemaDocument {
	@Id
	private String uid;
	private String schema;
	private JoyceSchemaMetadata metadata;
	private String type;
	private List<String> required;
	private String properties;
}
