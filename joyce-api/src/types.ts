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
	endpoint: string;
	collection: string;
	namespace: string;
	subtype: string;
}
export interface Schema {
	metadata: SchemaMetadata;
	properties: SchemaProperties;
}
export interface SchemaResources {
	[key: string]: Resource;
}
export interface Resource {
	source: string;
}
export interface ResponsableSchema extends Resource {
	label: string;
	schema: { metadata: SchemaMetadata; properties: SchemaProperties };
}

export interface JRPCParams {
	jsonrpc: string;
	method: string;
	params: any;
	id: string;
}

export interface Config {
	/** security config TBD */
	security: { [key:string]: string };
	/** default: true, enables jsonrpc channel (Kafka) */
	jsonrpc: boolean;
	/** schemas to be published by this api server */
	resources: {
		/** path to the resource */
		path: string;
		/** schema uid identifying the resource */
		schema: string;
	}[];
}
