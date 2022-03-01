require("module-alias/register");

import * as fs from "fs";
import * as path from "path";
import Handlebars from "handlebars";
import upperFirst from "lodash/upperFirst";
import camelCase from "lodash/camelCase";
import yaml from "js-yaml";
import crypto from "crypto";
import { graphqlMesh } from "@graphql-mesh/cli";

import type { Schema } from "src/types";
import { readConfig } from "@src/utils/config-util";
import { readRemoteSchemas, schemaToString, writeLocalSchemas } from "@src/utils/schema-utils";

const logger = require("pino")({ name: "configure-graphql" });

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "src/templates/schemas.json";
const WORKDIR = process.env.WORKDIR || "./workdir";
const mongoURI = process.env.MONGO_URI || "mongodb://localhost:27017/ingestion";

/**
 * configures graphql mesh reading this server config
 */
async function generateConfiguration() {
	await checkWorkdir();
	const config = await readConfig(SCHEMAS_SOURCE);
	const hasGraphQL = config.graphQL !== false;
	const hasRest = config.rest !== false;

	if (!hasGraphQL && !hasRest) {
		logger.info("Server Configuration minimal");
		await generateMeshrc([], mongoURI);
	} else {
		const schemas = await readRemoteSchemas(config);
		const hashes = calculateHashes(schemas);
		const hashCheck = await checkSavedHashes(hashes);

		if (!hashCheck) {
			await fs.promises.writeFile(`${WORKDIR}/hashes.json`, JSON.stringify(hashes, null, 4), "utf-8");
			await writeLocalSchemas(schemas, WORKDIR);
		}

		if (hasGraphQL && !hashCheck) {
			const models = await generateMoongooseModels(schemas);
			await generateMoongooseModelsEnhanced(schemas);
			await generateMeshrc(models, mongoURI);
		} else {
			await generateMeshrc([], mongoURI);
			logger.info("Mesh Configuration skipped");
		}
	}

	// mesh build
	return graphqlMesh();
}

function calculateHashes(schemas: { [x: string]: Schema }): { [x: string]: string } {
	return Object.keys(schemas).reduce(
		(acc, key) => ({
			...acc,
			[key]: crypto.createHash("sha256").update(schemaToString(schemas[key])).digest("base64"),
		}),
		{},
	);
}

async function checkSavedHashes(hashes: { [x: string]: string }): Promise<boolean> {
	return fs.promises.readFile(`${WORKDIR}/hashes.json`, "utf-8")
		.then((file) => JSON.parse(file))
		.then((readHashes) => (
			Object.keys(readHashes).length === Object.keys(hashes).length &&
			Object.keys(readHashes).every((key) => readHashes[key] === hashes[key])
		))
		.catch(() => false);
}

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
		sources: models.length ? [
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
		] : [],
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

(async () => {
	logger.info("Server Configuration started");
	await generateConfiguration().then(() => {
		logger.info("Server Configuration completed");
		// mesh dev/build + start
	});
})();

process.on("uncaughtException", (err) => {
	logger.error(err, `UncaughtException ${err}`);
});
process.on("unhandledRejection", (err) => {
	logger.error(err, `UnhandledRejection ${err}`);
});
