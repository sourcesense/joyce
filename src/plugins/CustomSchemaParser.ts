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
    this.required = Object.keys(this.properties).reduce((acc, label) => {
      const prop = this.properties[label];
      if (prop.required) {
        return [...acc, label];
      }
      return acc;
    }, []);
    this.nullable = Object.keys(this.properties).reduce((acc, label) => {
      const prop = this.properties[label];
      if (prop.nullable) {
        return [...acc, label];
      }
      return acc;
    }, []);
  }

  getSchemaProperties() {
    return Object.keys(this.properties).reduce((acc, label) => {
      let prp = {};
      console.log(label, this.nullable.includes(label));
      if (this.nullable.includes(label)) {
        prp = { type: [this.properties[label].type, "null"] };
      } else {
        prp = { type: this.properties[label].type };
      }
      return { ...acc, [label]: prp };
    }, {});
  }
}
