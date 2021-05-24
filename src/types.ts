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
  development?: boolean;
}
export interface Schema {
  $metadata: SchemaMetadata;
  properties: SchemaProperties;
}
export interface SchemaResources {
  [key: string]: Resource;
}
export interface Resource {
  version: string;
  source: string;
}
export interface ResponsableSchema extends Resource {
  label: string;
  schema: { $metadata: SchemaMetadata; properties: SchemaProperties };
}

export interface JRPCParams {
  jsonrpc: string;
  method: string;
  params: object;
  id: string;
}