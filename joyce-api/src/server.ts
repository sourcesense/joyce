import fastify from "fastify";
import * as fastifyStatic from "fastify-static";

import healthHandler from "./modules/health/routes";
import { CustomeSchemaParser } from "./plugins/CustomSchemaParser";
import fs from "fs";
// import path from "path";
import { SchemaConfiguration } from "./plugins/SchemaConfiguration";
import { MultipleEntitySchema, SingleEntitySchema } from "./plugins/PrepareFastifySchema";
import { JRPCParams, ResponsableSchema } from "./types";
import { readSchemas } from "./lib/schema-utils";
import { readConfig } from "./lib/config-util";
import { loadSchemas } from "./lib/openapi-utils";
const logger = require("pino")();
const path = require("path");

logger.info("starting server module");

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "schemas.json";
const WORKDIR = process.env.WORKDIR || "./workdir";
const PRODUCTION_URL = process.env.BASE_URL || "https://<production-url>";
const INTERNAL_URL = process.env.BASE_URL || "http://localhost:6650";
const HEALTH_PATH = process.env.HEALTH_PATH || "/health";
const JOYCE_GRAPHQL = process.env.JOYCE_GRAPHQL || true;

async function createServer(db, producer) {
	const JOYCE_API_KAFKA_COMMAND_TOPIC = process.env.JOYCE_API_KAFKA_COMMAND_TOPIC || "commands";

	return loadSchemas(SCHEMAS_SOURCE, WORKDIR).then((schemasList) => {
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
		server.get("/graphiql", (_, reply) => {
			reply.sendFile("graphiql.html");
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
		server.post<{ Body: JRPCParams }>(
			"/jrpc",
			{
				schema: {
					// @ts-ignore
					tags: ["jrpc"],
					body: {
						type: "object",
						required: ["jsonrpc", "method", "params", "id"],
						properties: {
							jsonrpc: { type: "string", enum: ["2.0"] },
							method: { type: "string" },
							params: { type: "object", minProperties: 1 },
							id: { type: "string", minLength: 1 },
						},
					},
					response: {
						200: { type: "string", enum: ["OK"] },
						500: { type: "string", enum: ["KO"] },
					},
				},
			},
			async function (req, res) {
				const payload = {
					topic: JOYCE_API_KAFKA_COMMAND_TOPIC,
					messages: JSON.stringify(req.body),
					key: req.body.id,
				};
				producer.send([payload], function (err: any) {
					if (err) {
						res.status(500).send("KO");
						return;
					}
					res.status(200).send("OK");
					return;
				});
			},
		);

		logger.info("preparing resources");
		schemasList.map((wrapper) => {
			const tempSchema = new CustomeSchemaParser(wrapper.schema);
			/**
	   Per specificare le interfacce dei parametri di Fastify usa:
		Querystring: {page:number:, size:number}
		Params: {id:string}
		Headers: {'x-joyce':string}
		Body: {veditu:string}
	   */
			server.get<{ Params: { id: string } }>(
				`/rest/${wrapper.path}/:id`,
				SingleEntitySchema(tempSchema),
				async function (req, res) {
					const { id: entityID } = req.params;
					try {
						const namespaced_collection = `${wrapper.schema.metadata.namespace || "default"}.${wrapper.schema.metadata.collection
						}`;
						const collection = db.collection(namespaced_collection);

						const _id = `joyce://content/${wrapper.schema.metadata.subtype}/${namespaced_collection}/${entityID}`;
						const entity = await collection.findOne({ _id });

						if (entity) {
							req.log.info(`entity ${_id} found`);
							res.status(200).send(entity);
						} else {
							req.log.info(`entity ${_id} not found`);
							res.status(404).send();
						}
					} catch (errore) {
						req.log.error(errore);
						res.status(500).send({ code: 500, message: errore.message });
					}
				},
			);
			server.get<{
				Querystring: {
					page: number | null;
					size: number | null;
					orderBy: "asc" | "desc";
					sortBy: string;
				};
			}>(`/rest/${wrapper.path}`, MultipleEntitySchema(tempSchema), async function (req, res) {
				const { page = 0, size = 10, orderBy, sortBy, ...other } = req.query;
				try {
					const namespaced_collection = `${wrapper.schema.metadata.namespace || "default"}.${wrapper.schema.metadata.collection
					}`;
					const collection = db.collection(namespaced_collection);
					const docs = await collection
						.find(other || {})
						.project(tempSchema.getSchemaPropertiesMongoProjection()) // è un ottimizzazione. Di fatto fastify già elimina le proprietà non comprese nello schema dichiarato poche righe sopra
						.sort({ [sortBy || "id"]: orderBy === "asc" ? 1 : -1 })
						.limit(size)
						.skip(page * size)
						.toArray();
					res.status(200).send(docs);
				} catch (errore) {
					req.log.error(errore);
					res.status(500).send(errore);
				}
			});
		});
		server.register(healthHandler, { prefix: HEALTH_PATH });

		server.setErrorHandler((error, req, res) => {
			req.log.error(error.toString());
			res.send({ error });
		});
		return server;
	});
}
export default createServer;
