import { Schema, SchemaProperties } from "../types";

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
