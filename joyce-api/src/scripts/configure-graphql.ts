require("module-alias/register");

import * as fs from "fs";
import * as path from "path";
import Handlebars from "handlebars";
import upperFirst from "lodash/upperFirst";
import camelCase from "lodash/camelCase";
import yaml from "js-yaml";

import type { Schema } from "src/types";
import { readConfig } from "@src/utils/config-util";
import { readRemoteSchemas, writeLocalSchemas } from "@src/utils/schema-utils";

const logger = require("pino")({ name: "configure-graphql" });

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "src/templates/schemas.json";
const WORKDIR = process.env.WORKDIR || "./workdir";
const mongoURI = process.env.MONGO_URI || "mongodb://localhost:27017/ingestion";

/**
 * generates and saves to WORKDIR moongoose models from templates/model.js.handlebars and remote schema
 */
async function generateMoongooseModels(config: { [x: string]: Schema }): Promise<string[]> {
	const generated = [];
	const template = await fs.promises.readFile("./src/templates/model.js.handlebars", "utf-8");
	const compiledTemplate = Handlebars.compile(template);
	// logger.info({ config }, "generating models from schemas");

	for (const key of Object.keys(config)) {
		const name = upperFirst(camelCase(key));
		logger.info(`generating ${name} model`);

		const text = compiledTemplate({
			data: JSON.stringify(config[key]),
			name,
			json_schema_module: path.resolve("./src/scripts/json-schema.js"),
		});
		await fs.promises.writeFile(`${WORKDIR}/${name}.model.js`, text, "utf-8");
		generated.push(name);
	}

	return Promise.resolve(generated);
}

async function generateMoongooseModelsEnhanced(config: { [x: string]: Schema }): Promise<void> {
	return Promise.resolve();
}

/**
 * generates and saves to WORKDIR a .meshrc.yml
 * @param config
 * @param mongoURI
 */
async function generateMeshrc(models: string[], mongoURI: string) {
	const meshConfig = {
		sources: [
			{
				name: "Mongoose",
				handler: {
					mongoose: {
						connectionString: mongoURI,
						models: models.map((name) => ({
							name,
							path: `./${name}.model.js`,
						})),
					},
				},
			},
		],
		require: ["ts-node/register/transpile-only"],
		serve: {
			customServerHandler: path.resolve("./src/scripts/mesh-server.js"),
		},
	};
	return fs.promises.writeFile(`${WORKDIR}/.meshrc.yml`, yaml.dump(meshConfig), "utf-8");
}

async function checkWorkdir() {
	return fs.promises.mkdir(WORKDIR, { recursive: true });
}

/**
 * configures graphql mesh reading this server config
 */
async function generateConfiguration() {
	await checkWorkdir();
	const config = await readConfig(SCHEMAS_SOURCE);
	const schemas = await readRemoteSchemas(config);
	await writeLocalSchemas(schemas, WORKDIR);
	const models = await generateMoongooseModels(schemas);
	await generateMoongooseModelsEnhanced(schemas);
	return generateMeshrc(models, mongoURI);
}

(async () => {
	await generateConfiguration().then(() => {
		logger.info("GRAPHQL Configuration completed");
	});
})();

process.on("uncaughtException", (err) => {
	logger.error(err, `UncaughtException ${err}`);
});
process.on("unhandledRejection", (err) => {
	logger.error(err, `UnhandledRejection ${err}`);
});
