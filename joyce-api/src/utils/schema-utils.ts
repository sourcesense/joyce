import * as fs from "fs";
import upperFirst from "lodash/upperFirst";
import camelCase from "lodash/camelCase";

import { getSchema } from "@clients/grpc";
import { readConfig } from "./config-util";
import type { Config, Schema } from "src/types";

const logger = require("pino")({ name: "schema-utils" });

export async function readRemoteSchemas(config: Config): Promise<{ [x: string]: Schema }> {
	const schemas = {};
	for (const resource of config.resources) {
		const schema = await getSchema(resource.schema);
		logger.debug({ resource }, "getting resource schema");

		schema.name = upperFirst(camelCase(resource.path));
		schema.metadata.endpoint = resource.path;
		schema.metadata.name = upperFirst(camelCase(resource.path));

		schemas[resource.path] = schema;

	}
	return schemas;
}

export async function writeLocalSchemas(schemas: { [x: string]: Schema }, destination: string): Promise<void> {
	for (const key of Object.keys(schemas)) {
		await fs.promises.writeFile(`${destination}/${schemas[key].name}.schema.json`, JSON.stringify(schemas[key], null, 4), "utf-8");
	}
	return Promise.resolve();
}

export async function readLocalSchemas(source, workdir): Promise<{ name: string; path: string; schema: Schema}[]> {
	const config = await readConfig(source);
	const schemas = [];

	for (const resource of config.resources) {
		const name = upperFirst(camelCase(resource.path));

		const promise = await fs.promises.readFile(`${workdir}/${name}.schema.json`, "utf-8")
			.then((content) => JSON.parse(content))
			.then((schema) => ({ name, path: resource.path, schema }));
		schemas.push(promise);
	}

	return schemas;
}
