import * as fastify from "fastify";
import * as fastifyStatic from "fastify-static";
import fp from "fastify-plugin";
import * as path from "path";

const logger = require("pino")({ name: "Joyce-API" });

import { readLocalSchemas } from "./utils/schema-utils";
import MongoOpenApiResource from "./plugins/MongoOpenApiResource";
import { readConfig } from "./utils/config-util";
import jrpcPlugin from "./plugins/JrpcPlugin";
import healthPlugin from "./plugins/HealthPlugin";

logger.info("starting server");

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "schemas.json";
const WORKDIR = process.env.WORKDIR || "./workdir";
const PRODUCTION_URL = process.env.BASE_URL || "https://<production-url>";
const INTERNAL_URL = process.env.BASE_URL || "http://localhost:6650";
// const HEALTH_PATH = process.env.HEALTH_PATH || "/health";
// const JOYCE_GRAPHQL = process.env.JOYCE_GRAPHQL || true;

async function createServer(db) {
	// const JOYCE_API_KAFKA_COMMAND_TOPIC = process.env.JOYCE_API_KAFKA_COMMAND_TOPIC || "commands";
	const config = await readConfig(SCHEMAS_SOURCE);
	const hasJrpc = config.jsonrpc === true;
	const hasRest = config.rest !== false;

	const server = fastify.default({
		logger,
	});
	server.register(require("fastify-cors"));

	server.register(fastifyStatic.default, {
		root: path.join(__dirname, "..", "static"),
		decorateReply: true,
		// prefix: "/public/", // optional: default '/'
	});
	server.get("/", (_, reply) => {
		reply.sendFile("index.html");
	});

	if (hasRest || hasJrpc) {
		server.register(require("fastify-oas"), {
			routePrefix: "/swagger-ui",
			exposeRoute: true,
			hideUntagged: true,
			swagger: {
				info: {
					title: "joyce-api",
					description: "api documentation",
					version: "1.3.0",
				},
				servers: [
					{ url: INTERNAL_URL, description: "local network" },
					{
						url: PRODUCTION_URL,
						description: "production",
					},
				],
				schemes: ["http"],
				consumes: ["application/json"],
				produces: ["application/json"],
			},
		});
	} else {
		logger.info("Swagger UI NOT enabled");
	}

	if (hasJrpc) {
		server.register(jrpcPlugin);
	} else {
		logger.info("Json RPC Channel NOT enabled");
	}

	server.register(healthPlugin, { prefix: "/health" });

	server.setErrorHandler((error, req, res) => {
		req.log.error(error.toString());
		res.send({ error });
	});

	return Promise.resolve()
		.then(() => {
			if (hasRest) {
				return readLocalSchemas(SCHEMAS_SOURCE, WORKDIR).then((schemasList) => {
					schemasList.map((wrapper) => {
						const resource = new MongoOpenApiResource(wrapper.schema, db);
						logger.info(`registering paths under ${wrapper.path} for ${wrapper.name}`);
						server.register(fp(resource.routes, { name: wrapper.name }));
					});
				});
			} else {
				logger.info("REST channel disabled");
			}
		})
		.then(() => server);
}
export default createServer;
