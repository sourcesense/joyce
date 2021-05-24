import { Schema, SchemaProperties } from "../types";

export class CustomeSchemaParser {
  readonly collectionName: string = "";
  readonly properties: SchemaProperties = {};
  readonly required: string[] = [];
  readonly nullable: string[] = [];
  readonly development: boolean = false;
  constructor({ schema: { $metadata, properties } }: { schema: Schema }) {
    this.development = $metadata.development || false;
    this.collectionName = $metadata?.collection;
    this.properties = properties;
  }

  parseProperties(properties, transformLabel = "") {
    const props = Object.keys(properties);
    const j = props.reduce((r, prop) => {
      if (properties[prop].type === "object") {
        return {
          ...r,
          ...this.parseProperties(properties[prop].properties, prop),
        };
      }
      return {
        ...r,
        [`${transformLabel ? `${transformLabel}.` : ""}${prop}`]:
          properties[prop],
      };
    }, {});
    return j;
  }

  getParsedProperties() {
    return this.parseProperties(this.properties, "");
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
