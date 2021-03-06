import { CustomeSchemaParser } from "./CustomSchemaParser";

const globaleQueryStringPagination = (entitySchema) => ({
	page: { type: "integer" },
	size: { type: "integer" },
	...entitySchema.getParsedProperties(),
	orderBy: {
		default: "desc",
		type: "string",
		items: {
			type: "string",
			enum: ["asc", "desc"],
		},
		uniqueItems: true,
		minItems: 1,
	},
	sortBy: {
		type: "string",
		enum: entitySchema.getSchemaToMongoProperties(),
	},
	/** ordinamento di multipli valori */
	// sortBy: {
	//   type: "array",
	//   items: {
	//     type: "string",
	//     enum: entitySchema.getSchemaToMongoProperties(),
	//   },
	//   uniqueItems: true,
	//   minItems: 1,
	// },
});

export function SingleEntitySchema(entitySchema: CustomeSchemaParser): any {
	const v = {
		schema: {
			// @ts-ignore
			tags: ["custom"],
			params: {
				type: "object",
				properties: {
					id: { type: "string" },
				},
			},
			response: {
				200: {
					type: "object",
					properties: entitySchema.getSchemaProperties(),
				},
			},
		},
	};

	return v;
}

export function MultipleEntitySchema(
	entitySchema: CustomeSchemaParser
): any {
	return {
		schema: {
			querystring: globaleQueryStringPagination(entitySchema),
			// @ts-ignore
			tags: ["custom"],
			response: {
				200: {
					type: "array",
					items: {
						type: "object",
						properties: entitySchema.getSchemaProperties(),
					},
				},
			},
		},
	};
}
