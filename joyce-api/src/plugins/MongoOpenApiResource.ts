import { parseProperties } from "@src/utils/openapi-utils";
import { Schema } from "@src/types";
import { FastifyPluginCallback } from "fastify";
import type { Collection, Db } from "mongodb";

export default class MongoOpenApiResource {
	readonly schema: Schema;
	readonly collection: Collection;
	readonly idPrefix: string;
	private parsedProperties;

	constructor(schema: Schema, db: Db) {
		this.schema = schema;
		const namespaced_collection = `${schema.metadata.namespace || "default"}.${schema.metadata.collection}`;
		this.idPrefix = `joyce://content/${schema.metadata.subtype.toLocaleLowerCase()}/${namespaced_collection}`;
		this.collection = db.collection(namespaced_collection);
	}

	routes:FastifyPluginCallback<unknown> = async (fastify, options, done) => {
		/*
			Per specificare le interfacce dei parametri di Fastify usa:
			Querystring: {page:number:, size:number}
			Params: {id:string}
			Headers: {'x-joyce':string}
			Body: {veditu:string}
		*/

		fastify.get<{ Params: { id: string } }>(
			this.getResourcePath(),
			this.getResourceSchema(),
			async (req, res) => {
				const { id: entityID } = req.params;
				const _id = `${this.idPrefix}/${entityID}`;
				return this.collection.findOne({ _id }).then((entity) => {
					if (entity) {
						req.log.info(`entity ${_id} found`);
						res.status(200).send(entity);
					} else {
						req.log.info(`entity ${_id} not found`);
						res.status(404).send();
					}
				}).catch((error) => {
					req.log.error(error);
					res.status(500).send({ code: 500, message: error.message });
				});
			},
		);

		fastify.get<{
			Querystring: {
				page: number | null;
				size: number | null;
				orderBy: "asc" | "desc";
				sortBy: string;
			};
		}>(
			this.getListPath(),
			this.getListSchema(),
			async (req, res) => {
				const { page = 0, size = 10, orderBy, sortBy, ...other } = req.query;
				try {
					const docs = await this.collection
						.find(other || {})
						.project(this.getMongoProjection()) // è un ottimizzazione. Di fatto fastify già elimina le proprietà non comprese nello schema dichiarato poche righe sopra
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
		done();
	}

	getResourcePath():string {
		return `${this.getListPath()}/:id`;
	}

	getResourceSchema = () => {
		return {
			schema: {
				tags: ["custom"],
				params: {
					type: "object",
					properties: {
						id: { type: "string" },
					}
				},
				response: {
					200: {
						type: "object",
						properties: this.getSchemaProperties(),
					}
				}
			}
		};
	}

	getListPath(): string {
		return `/rest/${this.schema.metadata.endpoint}`;
	}

	getListSchema = () => {
		return {
			schema: {
				querystring: {
					page: { type: "integer" },
					size: { type: "integer" },
					...this.getParsedProperties(),
					orderBy: {
						default: "desc",
						type: "string",
						items: {
							type: "string",
							enum: ["asc", "desc"],
						},
						uniqueItems: true,
						minItems: 1,
					},
					sortBy: {
						type: "string",
						enum: this.getMongoProperties(),
					}
				},
				tags: ["custom"],
				response: {
					200: {
						type: "array",
						items: {
							type: "object",
							properties: this.getSchemaProperties(),
						}
					}
				}
			}
		};
	}

	getParsedProperties = () => {
		if (!this.parsedProperties) {
			this.parsedProperties = parseProperties(this.schema.properties, "");
		}
		return this.parsedProperties;
	}

	getSchemaProperties() {
		return this.schema.properties;
	}

	getMongoProperties() {
		return Object.keys(this.schema.properties);
	}

	getMongoProjection() {
		return this.getMongoProperties()
			.reduce((r, prop) => ({ ...r, [prop]: 1 }), {});
	}
}
