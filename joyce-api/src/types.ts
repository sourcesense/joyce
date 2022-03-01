import { JoyceUriSubtype } from "@generated/grpc/enumeration/joyce_uri_subtype_pb";

declare module "fastify" {
	interface FastifySchema {
		tags?: string[];
	}
}

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
	endpoint?: string;
	name?: string;
	collection: string;
	namespace: string;
	subtype: keyof typeof JoyceUriSubtype;
	indexes: {[x:string]: number}[];
	development: boolean;
	store: boolean;
	validation: boolean;
	indexed: boolean;
	connectors: boolean;
	pb_export: boolean;
	extra: string;
}
export interface Schema {
	uid: string;
	$schema: string;
	type: string;
	metadata: SchemaMetadata;
	properties: SchemaProperties;
	required: string[];
	name?: string;
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
	/** default: true, enables graphQL channel */
	graphQL: boolean;
	/** default: true, enables rest channel */
	rest: boolean;
	/** schemas to be published by this api server */
	resources: {
		/** path to the resource */
		path: string;
		/** schema uid identifying the resource */
		schema: string;
	}[];
}
