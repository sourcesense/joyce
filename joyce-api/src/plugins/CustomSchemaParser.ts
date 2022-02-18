import { ResponsableSchema, SchemaProperties } from "../types";
// import { Schema } from "@generated/grpc/model/schema_pb";

type RichSchema = {
	properties: string;
	metadata: {
		endpoint: string;
	}
}

export class CustomeSchemaParser {
	readonly endpoint: string = "";
	readonly properties: SchemaProperties = {};
	readonly required: string[] = [];
	readonly nullable: string[] = [];
	constructor({ schema: { metadata, properties } }: ResponsableSchema) {
		this.endpoint = metadata?.endpoint;
		this.properties = properties as SchemaProperties;
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
