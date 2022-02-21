import fetch from "node-fetch";
import { Config, ResponsableSchema, SchemaResources } from "../types";
import upperFirst from "lodash/upperFirst";
import camelCase from "lodash/camelCase";
import { getSchema } from "@clients/grpc";

const logger = require("pino")({ name: "schema-configuration" });

export class SchemaConfiguration {
	readonly sources = [];
	constructor({ resources }: Config) {
		logger.info({ resources }, "creating schema configuration");
		resources.map(({ path, schema }) => {
			this.sources.push({
				label: path,
				source: schema,
			});
		});
	}
	requestSchemas(logger): Promise<ResponsableSchema>[] {
		return this.sources.map((resource) => {
			logger.info(resource.source);
			return getSchema(resource.source)
				.then((j) => {
					logger.info(`SUCCESS: ${resource.label} schema Found`);
					// j["$metadata"]["endpoint"] = resource.label;
					// j["$metadata"]["name"] = _.upperFirst(_.camelCase(j["$metadata"]["name"]));
					// j.name = j["$metadata"]["name"];

					const name = upperFirst(camelCase(resource.label));

					return {
						...resource,
						name,
						schema: {
							...j,
							metadata: {
								...j.metadata,
								endpoint: resource.label,
								name,
							},
						},
					};
				})
				.catch((e) => {
					const { statusText } = e;
					logger.error(`ERROR:  ${resource.label} ${statusText} ${e}`);
					return {};
				});
		});
	}
}
