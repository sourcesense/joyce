export interface SchemaProperty {
  required?: boolean;
  type: string;
  nullable?: boolean;
}
export interface SchemaProperties {
  [key: string]: SchemaProperty;
}
export interface SchemaMetadata {
  uid: string;
  collection: string;
  cassandra_schema: string;
  subtype: string;
}
export interface Schema {
  $metadata: SchemaMetadata;
  properties: SchemaProperties;
}

export class CustomeSchemaParser {
  readonly collectionName: string = "";
  readonly properties: SchemaProperties = {};
  readonly required: string[] = [];
  readonly nullable: string[] = [];
  constructor({ schema: { $metadata, properties } }: { schema: Schema }) {
    this.collectionName = $metadata?.collection;
    this.properties = properties;
  }

  getSchemaProperties() {
    return this.properties;
  }
  getSchemaToMongoProperties() {
    return Object.keys(this.properties);
  }
  getSchemaPropertiesMongoProjection() {
    const props = Object.keys(this.properties);
    return props.reduce((r, prop) => ({ ...r, [prop]: 1 }), {});
  }
}
