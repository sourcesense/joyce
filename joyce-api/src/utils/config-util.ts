import * as fs from "fs";

import type { Config } from "src/types";

/**
 * @returns the content of the config of this api server
 */
export function readConfig(source: string): Promise<Config> {
	return fs.promises
		.readFile(source, {
			encoding: "utf8",
		})
		.then((file) => JSON.parse(file) as Config)
		.then((config) => {
			validate(config);
			return config;
		});
}

function validate(config: Config): void {
	if (Array.isArray(config.resources)) {
		const validResources = config.resources.every((resource) => {
			return resource.path && resource.schema;
		});
		if (!validResources) {
			throw new Error("resources config must have paths and schemas");
		}
		const paths = config.resources.reduce((acc, res) => {
			return { ...acc, [res.path]: (acc[res.path] || 0) + 1 };
		}, {});
		const hasDuplicatePaths = Object.keys(paths).length < config.resources.length;
		if (hasDuplicatePaths) {
			throw new Error("paths should be unique");
		}
	} else {
		throw new Error("no configured resources");
	}
	if (config.jsonrpc !== true && config.graphQL === false && config.rest === false) {
		throw new Error("no configured output");
	}
}
