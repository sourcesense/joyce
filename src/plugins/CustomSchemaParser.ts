interface SchemaParserProperty {
  required?: boolean;
  type: string;
  nullable?: boolean;
}
interface SchemaParserProperties {
  [key: string]: SchemaParserProperty;
}
interface SchemaMetadata {
  message_key: object;
  uid: string;
  collection: string;
  cassandra_schema: string;
}

export class CustomeSchemaParser {
  readonly collectionName: string = "";
  readonly properties: SchemaParserProperties = {};
  readonly required: string[] = [];
  readonly nullable: string[] = [];
  constructor(schema: {
    $metadata: SchemaMetadata;
    properties: SchemaParserProperties;
  }) {
    this.collectionName = schema["$metadata"].collection;
    this.properties = schema.properties;
  }

  getSchemaProperties() {
    return this.properties;
  }
  getSchemaToMongoProperties() {
    return Object.keys(this.properties);
  }
}
