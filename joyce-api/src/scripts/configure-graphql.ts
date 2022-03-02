require("module-alias/register");

import * as fs from "fs";
import * as path from "path";
import Handlebars from "handlebars";
import upperFirst from "lodash/upperFirst";
import camelCase from "lodash/camelCase";
import yaml from "js-yaml";
import crypto from "crypto";
import { graphqlMesh } from "@graphql-mesh/cli";

import type { Schema, Config } from "src/types";
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

	if (hasGraphQL || hasRest) {
		const schemas = await readRemoteSchemas(config);
		const hash = calculateHash(schemas, config);
		const hashCheck = await checkSavedHash(hash);

		// const hashes = calculateHashes(schemas);
		// const hashCheck2 = await checkSavedHashes(hashes);
		// logger.info({ hashCheck, hashCheck2 }, "hash check result");

		if (!hashCheck) {
			await fs.promises.writeFile(`${WORKDIR}/hash.txt`, hash, "utf-8");
			// await fs.promises.writeFile(`${WORKDIR}/hashes.json`, JSON.stringify(hashes, null, 4), "utf-8");
			await writeLocalSchemas(schemas, WORKDIR);
			if (hasGraphQL) {
				const models = await generateMoongooseModels(schemas);
				await generateMoongooseModelsEnhanced(schemas);
				await generateMeshrc(models, mongoURI);
				logger.info("Mesh Configuration full");
				await graphqlMesh();
			}
		}
	}

	if (!hasGraphQL) {
		await generateMeshrc([], mongoURI);
		logger.info("Mesh Configuration minimal");
		await graphqlMesh();
	}

	// mesh build
	return Promise.resolve();
}

function calculateHash(schemas: Record<string, Schema>, config: Config): string {
	const keys = Object.keys(schemas);
	keys.sort();
	const relevantInfo = [config.graphQL, ...keys.map((k) => createHash(schemaToString(schemas[k])))];
	return createHash(JSON.stringify(relevantInfo));
}

function createHash(content: string): string {
	return crypto.createHash("sha256").update(content).digest("base64");
}

async function checkSavedHash(hash: string): Promise<boolean> {
	return fs.promises.readFile(`${WORKDIR}/hash.txt`, "utf-8")
		.then((saved) => saved === hash)
		.catch(() => fs.promises.writeFile(`${WORKDIR}/hash.txt`, hash, "utf-8")
			.then(() => false)
		);
}

/**
 * generates and saves to WORKDIR moongoose models from templates/model.js.handlebars and remote schema
 */
async function generateMoongooseModels(schemas: Record<string, Schema>): Promise<string[]> {
	const generated = [];
	const template = await fs.promises.readFile("./src/templates/model.js.handlebars", "utf-8");
	const compiledTemplate = Handlebars.compile(template);
	// logger.info({ config }, "generating models from schemas");

	for (const key of Object.keys(schemas)) {
		const name = upperFirst(camelCase(key));
		logger.info(`generating ${name} model`);

		const text = compiledTemplate({
			data: JSON.stringify(schemas[key]),
			name,
			date: (new Date()).toISOString(),
			json_schema_module: path.resolve("./src/scripts/json-schema.js"),
		});
		await fs.promises.writeFile(`${WORKDIR}/${name}.model.js`, text, "utf-8");
		generated.push(name);
	}

	return Promise.resolve(generated);
}

async function generateMoongooseModelsEnhanced(schemas: Record<string, Schema>): Promise<void> {
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
	const start = Date.now();
	logger.info("Server Configuration started");
	await generateConfiguration().then(() => {
		logger.info(`Server Configuration completed in ${Date.now() - start}ms`);
		// mesh dev/build + start
	});
})();

process.on("uncaughtException", (err) => {
	logger.error(err, `UncaughtException ${err}`);
});
process.on("unhandledRejection", (err) => {
	logger.error(err, `UnhandledRejection ${err}`);
});
