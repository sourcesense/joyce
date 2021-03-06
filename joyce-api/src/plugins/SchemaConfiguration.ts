import fetch from "node-fetch";
import { ResponsableSchema, SchemaResources } from "../types";
import _ from "lodash";

export class SchemaConfiguration {
	readonly sources = [];
	constructor({ schemas }: { schemas: SchemaResources }) {
		Object.keys(schemas).map((label) => {
			this.sources.push({
				label,
				...schemas[label],
			});
		});
	}
	requestSchemas(logger): Promise<ResponsableSchema>[] | [] {
		return this.sources.map((resource) => {
			logger.info(resource.source);
			return fetch(resource.source)
				.then((r) => {
					if (!r.ok) {
						throw r;
					}
					return r.json();
				})
				.then((j) => {
					logger.info(`SUCCESS: ${resource.label} schema Found`);
					j["$metadata"]["endpoint"] = resource.label;
					j["$metadata"]["name"] = _.upperFirst(_.camelCase(j["$metadata"]["name"]));
					j.name = j["$metadata"]["name"];

					return {
						...resource,
						schema: j,
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
