import _ from "lodash";
import { Schema, SchemaDefinitionProperty } from "mongoose";

/*
 * From https://github.com/simplyhexagonal/json-schema-to-mongoose-schema
 * v1.2.2
 * modified to support type: [keyof typeof schemaTypeMap, null]
*/
// @ts-ignore
// export { version } from "../package.json";

export const schemaTypeMap = {
	string: String,
	boolean: Boolean,
	number: Number,
	integer: Number,
};



type SchemaTypeType = (keyof typeof schemaTypeMap | "null") | ((keyof typeof schemaTypeMap) | "null")[];

export interface JsonSchemaBaseType {
	type: SchemaTypeType;
	pattern?: string;
	minLength?: number;
	maxLength?: number;
	minimum?: number;
	maximum?: number;
	enum?: string[];
	format?: string;
}

export interface JsonSchemaArrayType {
	type: "array";
	minItems?: number;
	maxItems?: number;
	items: JsonSchemaValidTypes | JsonSchemaAnyOfType;
}

export interface JsonSchemaObjectType {
	type: "object";
	properties: {
		[k: string | symbol]: JsonSchemaValidTypes | JsonSchemaAnyOfType;
	};
	required?: string[];
}

export type JsonSchemaValidTypes = JsonSchemaBaseType | JsonSchemaObjectType | JsonSchemaArrayType;

export interface JsonSchemaAnyOfType {
	anyOf: JsonSchemaObjectType[];
}

export interface JsonSchema {
	$schema: string;
	definitions: { [k: string | symbol]: JsonSchemaObjectType };
}

export type Validation = [(val: unknown[]) => boolean, string] | [];

export const genArrayLimit = (minItems?: number, maxItems?: number): Validation => {
	const hasMin = typeof minItems === "number";
	const hasMax = typeof maxItems === "number";

	if (hasMin && hasMax) {
		return [
			(val: any[]) => val.length >= minItems && val.length <= maxItems,
			`{PATH} is not within range (${minItems} to ${maxItems} items)`,
		];
	} else if (hasMin) {
		return [(val: any[]) => val.length >= minItems, `{PATH} must have at least ${minItems} items`];
	} else if (hasMax) {
		return [(val: any[]) => val.length <= maxItems, `{PATH} exceeds the limit of ${maxItems} items`];
	}

	return [];
};

function mapSchemaTypeType(original: SchemaTypeType, format: string): any {
	if (Array.isArray(original)) {
		const mapped = original
			.filter((k) => k !== "null")
			.map((t) => (format === "date-time" ? Date : schemaTypeMap[t]));
		if (mapped.length === 1) {
			return mapped[0];
		}
		throw new Error(`multiple types not allowed, received ${original.join(", ")}`);
	}
	return format === "date-time" ? Date : schemaTypeMap[original];
}

function hasOwnProperty(obj: Record<string | symbol, unknown> | unknown, key: string | string[]): boolean {
	if (Array.isArray(key)) {
		return key
			.filter((k) => k !== "null")
			.every((k) => Object.prototype.hasOwnProperty.call(obj, k));
	}
	return Object.prototype.hasOwnProperty.call(obj, key);
}

export const typeHandler = (schemaType: JsonSchemaBaseType, required?: boolean) => {
	const {
		type: schemaTypeType,
		pattern,
		minLength,
		maxLength,
		minimum: min,
		maximum: max,
		format,
		enum: enumValues,
	} = schemaType;

	if (hasOwnProperty(schemaTypeMap, schemaTypeType)) {
		return {
			type: mapSchemaTypeType(schemaTypeType, format),
			match: pattern ? new RegExp(pattern) : new RegExp(/.*/gim),
			minLength,
			maxLength,
			min,
			max,
			enum: enumValues,
			required,
		};
	}

	throw new Error(`Unsupported schema type: ${schemaType}`);
};

export const processAnyOf = (property: JsonSchemaAnyOfType) => {
	const { anyOf } = property;

	if (!_.isArray(anyOf)) {
		throw new Error("Invalid JSON Schema, expected anyOf to be an array");
	}

	if (anyOf.length === 0) {
		throw new Error("Invalid JSON Schema, expected anyOf to have at least one item");
	}

	if (anyOf.length === 1) {
		throw new Error("Invalid JSON Schema, expected anyOf to have more than one item");
	}

	const onlyObjects = anyOf.reduce((a, b) => {
		return a && b.type === "object";
	}, true);

	if (!onlyObjects) {
		throw new Error("Invalid JSON Schema, expected anyOf to only contain objects");
	}

	return anyOf.reduce(
		(a, b) => {
			_.forEach(b.properties, (value: unknown, key: string) => {
				let subProperty: JsonSchemaValidTypes;

				if ((value as JsonSchemaAnyOfType).anyOf) {
					subProperty = processAnyOf(value as JsonSchemaAnyOfType);
				} else {
					subProperty = value as JsonSchemaValidTypes;
				}

				if (!hasOwnProperty(a.properties, key)) {
					a.properties[key] = subProperty;
				} else {
					if ((a.properties[key] as JsonSchemaValidTypes).type !== subProperty.type) {
						throw new Error(
							`Invalid JSON Schema, expected anyOf to only contain objects with identical properties`,
						);
					}

					a.properties[key] = _.mergeWith(a.properties[key], subProperty, (objValue, srcValue) => {
						if (_.isArray(objValue)) {
							return objValue.concat(srcValue);
						}

						if (_.isPlainObject(objValue)) {
							return _.merge(objValue, srcValue);
						}

						if (objValue !== srcValue) {
							throw new Error(
								`Invalid JSON Schema, values of types other than object or array must be identical`,
							);
						}

						return objValue;
					});
				}
			});

			a.required = (a.required || []).concat(b.required || []);

			return a;
		},
		{
			type: "object",
			properties: {},
			required: [],
		} as JsonSchemaObjectType,
	);
};

export const handleProperty = (
	key: string,
	property: JsonSchemaBaseType | JsonSchemaObjectType | JsonSchemaArrayType | JsonSchemaAnyOfType,
	subSchemaHandler: (key: string, subSchema: JsonSchemaObjectType | JsonSchemaArrayType, required?: string[]) => void,
	subTypeHandler: (key: string, schemaType: JsonSchemaBaseType) => void,
) => {
	if (!_.isPlainObject(property)) {
		throw new Error(`Invalid JSON Schema, ${key} is not an object`);
	}

	if (!hasOwnProperty(property, "type") && !hasOwnProperty(property, "anyOf")) {
		throw new Error(`Invalid JSON Schema, ${key} is missing type`);
	}

	let finalProperty: JsonSchemaValidTypes;

	if (hasOwnProperty(property, "anyOf")) {
		finalProperty = processAnyOf(property as JsonSchemaAnyOfType);
	} else {
		finalProperty = property as JsonSchemaValidTypes;
	}

	if (/array|object/.test(finalProperty.type.toString())) {
		subSchemaHandler(key, finalProperty as JsonSchemaObjectType | JsonSchemaArrayType);
		return;
	}

	subTypeHandler(key, finalProperty as JsonSchemaBaseType);
};

export const traverseDefinitions = (
	definitions: JsonSchema["definitions"] | JsonSchemaValidTypes | JsonSchemaAnyOfType,
	definitionKey?: string,
) => {
	const schema = new Schema();

	const schemaDefinition = definitionKey
		? (definitions as JsonSchema["definitions"])[definitionKey]
		: hasOwnProperty(definitions, "anyOf")
			? processAnyOf(definitions as JsonSchemaAnyOfType)
			: (definitions as JsonSchemaValidTypes);

	if (!_.isPlainObject(schemaDefinition)) {
		throw new Error(`Invalid JSON Schema, definition is not an object`);
	}

	if (!hasOwnProperty(schemaDefinition, "type")) {
		throw new Error(`Invalid JSON Schema, missing type while traversing definitions`);
	}

	if (definitionKey && schemaDefinition.type !== "object") {
		throw new Error(`Invalid JSON Schema, expected type to be 'object' for ${definitionKey}`);
	}

	if (schemaDefinition.type === "object") {
		const subSchemas: [string, JsonSchemaObjectType | JsonSchemaArrayType][] = [];

		_.forEach((schemaDefinition as JsonSchemaObjectType).properties || [], (p, key) => {
			handleProperty(
				key,
				p,
				(key, subSchema) => {
					subSchemas.push([key, subSchema]);
				},
				(key, schemaType) => {
					schema.add({
						[key]: typeHandler(
							schemaType,
							_.includes((schemaDefinition as JsonSchemaObjectType).required || [], key),
						) as SchemaDefinitionProperty<any>,
					});
				},
			);
		});

		_.forEach(subSchemas, ([key, subSchema]) => {
			const subSchemaType =
				subSchema.type === "array"
					? [traverseDefinitions(subSchema.items as JsonSchemaValidTypes)]
					: traverseDefinitions(subSchema);

			const validate = genArrayLimit(
				(subSchema as JsonSchemaArrayType).minItems,
				(subSchema as JsonSchemaArrayType).maxItems,
			);

			schema.add({
				[key]: {
					type: subSchemaType,
					required: _.includes((schemaDefinition as JsonSchemaObjectType).required || [], key),
					validate,
				},
			});
		});

		return schema;
	}

	if (schemaDefinition.type === "array") {
		schema.add({
			type: traverseDefinitions(schemaDefinition.items as JsonSchemaValidTypes),
		});

		return schema;
	}

	return typeHandler(schemaDefinition, false) as SchemaDefinitionProperty<any>;
};

const jsonSchemaToMongooseSchema = (jsonSchema: JsonSchema, definitionKey: string) => {
	if (!/draft-(0[4-9]|[1-9][0-9])/.test(_.get(jsonSchema, "$schema", "draft-00"))) {
		throw new Error("Invalid JSON Schema, expected $schema to be draft-04 or greater");
	}

	const definitions = jsonSchema.definitions;

	if (_.isPlainObject(definitions) && hasOwnProperty(definitions, definitionKey)) {
		const schema = traverseDefinitions(definitions, definitionKey);

		return schema as Schema;
	}

	throw new Error("Invalid JSON Schema");
};

export default jsonSchemaToMongooseSchema;
