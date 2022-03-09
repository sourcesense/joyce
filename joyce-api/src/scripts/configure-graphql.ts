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

const CONFIG_SOURCE = process.env.CONFIG_SOURCE || "./api-config.json";
const WORKDIR = process.env.WORKDIR || "./workdir";
const mongoURI = process.env.MONGO_URI || "mongodb://localhost:27017/ingestion";

/**
 * according to current configuration, prepares graphql mesh and builds/starts it if needed
 */
async function generateConfiguration() {
	await checkWorkdir();
	const config = await readConfig(CONFIG_SOURCE);
	const hasGraphQL = config.graphQL !== false;
	const hasRest = config.rest !== false;

	if (hasGraphQL || hasRest) {
		const schemas = await readRemoteSchemas(config);
		const hash = createResourcesHash(schemas, config);
		const hashCheck = await checkAndPersistHash(hash, "resources.hash");

		if (!hashCheck) {
			await writeLocalSchemas(schemas, WORKDIR);
			if (hasGraphQL) {
				const models = await generateMoongooseModels(schemas);
				const meshConfig = await generateMeshrc(models, mongoURI);
				logger.info("Mesh Configuration full");

				const sameMesh = await checkAndPersistHash(createHash(meshConfig), "mesh.hash");
				if (!sameMesh) {
					return await graphqlMesh();
				} else {
					logger.info("Mesh build skipped");
				}
			}
		}
	}

	if (!hasGraphQL) {
		const meshConfig = await generateMeshrc([], mongoURI);
		logger.info("Mesh Configuration minimal");
		const sameMesh = await checkAndPersistHash(createHash(meshConfig), "mesh.hash");
		if (!sameMesh) {
			return await graphqlMesh();
		} else {
			logger.info("Mesh build skipped");
		}
	}

	// mesh build
	return Promise.resolve();
}

/**
 * Calculates the cumulative hash of schemas and graphql channel enabling a change of this hash toggles a mesh build.
 * It is critical when starting using "start" script and a persisted workdir.
 * Ininfluent with "start:dev" or "start:live" (volatile workdir).
 *
 * @param schemas
 * @param config
 * @returns
 */
function createResourcesHash(schemas: Record<string, Schema>, config: Config): string {
	const keys = Object.keys(schemas);
	keys.sort();
	const relevantInfo = [config.graphQL, ...keys.map((k) => createHash(schemaToString(schemas[k])))];
	return createHash(JSON.stringify(relevantInfo));
}

/**
 * given a string returns its hash
 */
function createHash(content: string): string {
	return crypto.createHash("sha256").update(content).digest("base64");
}

/**
 * verifies if there's a saved hash and compares with the current, and saves the latest
 * @param hash
 * @param filename
 * @returns if the persisted hash is equal to the new one
 */
async function checkAndPersistHash(hash: string, filename: string): Promise<boolean> {
	return fs.promises.readFile(`${WORKDIR}/${filename}`, "utf-8")
		.then((saved) => saved === hash)
		.then((result) => fs.promises.writeFile(`${WORKDIR}/${filename}`, hash, "utf-8")
			.then(() => result)
		)
		.catch(() => fs.promises.writeFile(`${WORKDIR}/${filename}`, hash, "utf-8")
			.then(() => false)
		);
}

/**
 * generates and saves to WORKDIR moongoose models from templates/model.js.handlebars and remote schema
 */
async function generateMoongooseModels(schemas: Record<string, Schema>): Promise<string[]> {
	const generated = [];
	const template = await fs.promises.readFile("./src/templates/model.hbs", "utf-8");
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

/**
 * generates and saves to WORKDIR a .meshrc.yml
 * @param config
 * @param mongoURI
 */
async function generateMeshrc(models: string[], mongoURI: string): Promise<string> {
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
	const text = yaml.dump(meshConfig);
	return fs.promises.writeFile(`${WORKDIR}/.meshrc.yml`, text, "utf-8").then(() => text);
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
