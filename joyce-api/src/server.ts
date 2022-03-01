import fastify from "fastify";
import * as fastifyStatic from "fastify-static";
import path from "path";
const logger = require("pino")();

import healthHandler from "./modules/health/routes";
import { readLocalSchemas } from "./utils/schema-utils";
import MongoOpenApiResource from "./plugins/MongoOpenApiResource";
import { readConfig } from "./utils/config-util";
import jrpcPlugin from "./plugins/JrpcPlugin";

logger.info("starting server module");

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "schemas.json";
const WORKDIR = process.env.WORKDIR || "./workdir";
const PRODUCTION_URL = process.env.BASE_URL || "https://<production-url>";
const INTERNAL_URL = process.env.BASE_URL || "http://localhost:6650";
// const HEALTH_PATH = process.env.HEALTH_PATH || "/health";
// const JOYCE_GRAPHQL = process.env.JOYCE_GRAPHQL || true;

async function createServer(db) {
	// const JOYCE_API_KAFKA_COMMAND_TOPIC = process.env.JOYCE_API_KAFKA_COMMAND_TOPIC || "commands";
	const config = await readConfig(SCHEMAS_SOURCE);

	return readLocalSchemas(SCHEMAS_SOURCE, WORKDIR).then((schemasList) => {
		// logger.info({ schemasList }, "schemaslist");
		// const filteredSchemalist = schemasList.filter((e) => e.label);
		const server = fastify({
			logger: {
				level: "info",
			},
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

		server.register(require("fastify-oas"), {
			routePrefix: "/docs",
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

		if (config.jsonrpc === true) {
			server.register(jrpcPlugin);
		} else {
			logger.info("Json RPC Channel NOT enabled");
		}

		schemasList.map((wrapper) => {
			const resource = new MongoOpenApiResource(wrapper.schema, db);
			server.register(resource.routes);
		});
		server.register(healthHandler, { prefix: "/health" });

		server.setErrorHandler((error, req, res) => {
			req.log.error(error.toString());
			res.send({ error });
		});
		return server;
	});
}
export default createServer;
