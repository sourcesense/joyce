import fastify from "fastify";
import healthHandler from "./modules/health/routes";
import { CustomeSchemaParser } from "./plugins/CustomSchemaParser";
import fs from "fs";
// import path from "path";
import { SchemaConfiguration } from "./plugins/SchemaConfiguration";
import {
  MultipleEntitySchema,
  SingleEntitySchema,
} from "./plugins/PrepareFastifySchema";
import { ResponsableSchema } from "./types";

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "assets/schemas.json";
const PRODUCTION_URL = process.env.BASE_URL || "https://<production-url>";
const INTERNAL_URL = process.env.BASE_URL || "http://localhost:3000";
const HEALTH_PATH = process.env.HEALTH_PATH || "/health";
// const schemaSources = fs.readFileSync(
//   path.join(__dirname, SCHEMAS_SOURCE),
//   "utf8"
// );
const schemaSources = fs.readFileSync(SCHEMAS_SOURCE, "utf8");
const schemaConfiguration = new SchemaConfiguration(JSON.parse(schemaSources));
const requests = schemaConfiguration.requestSchemas();

function createServer(db) {
  return Promise.all(requests).then((schemasList) => {
    const filteredSchemalist = schemasList.filter((e) => e.label);
    const server = fastify({
      logger: {
        level: "info",
      },
    });
    server.register(require("fastify-cors"));
    server.register(require("fastify-oas"), {
      routePrefix: "/docs",
      exposeRoute: true,
      swagger: {
        info: {
          title: "nile-rest-api",
          description: "api documentation",
          version: "1.0.0",
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
    filteredSchemalist.map((schema: ResponsableSchema) => {
      const tempSchema = new CustomeSchemaParser(schema);
      /**
       Per specificare le interfacce dei parametri di Fastify usa:
        Querystring: {page:number:, size:number}
        Params: {id:string}
        Headers: {'x-nile':string}
        Body: {veditu:string}
       */
      server.get<{ Params: { id: string } }>(
        `/${tempSchema.collectionName}/:id`,
        SingleEntitySchema(tempSchema),
        async function (req, res) {
          const { id: entityID } = req.params;
          try {
            const collection = db.collection(
              schema.schema.$metadata.collection
            );
            const _id = `nile://content/${schema.schema.$metadata.subtype}/${schema.schema.$metadata.collection}/${entityID}`;
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
        }
      );
      server.get<{
        Querystring: {
          page: number | null;
          size: number | null;
          orderBy: "asc" | "desc";
          sortBy: string;
        };
      }>(
        `/${tempSchema.collectionName}`,
        MultipleEntitySchema(tempSchema),
        async function (req, res) {
          const { page = 0, size = 10, orderBy, sortBy, ...other } = req.query;

          console.log("aaaaaaaa", other);
          try {
            const collection = db.collection(
              schema.schema.$metadata.collection
            );
            const docs = await collection
              .find(other || {})
              .project(tempSchema.getSchemaPropertiesMongoProjection()) //è un ottimizzazione. Di fatto fastify già elimina le proprietà non comprese nello schema dichiarato poche righe sopra
              .sort({ [sortBy || "id"]: orderBy === "asc" ? 1 : -1 })
              .limit(size)
              .skip(page * size)
              .toArray();
            res
              .status(200)
              .header("x-nile-schema-version", schema.version)
              .send(docs);
          } catch (errore) {
            req.log.error(errore);
            res.status(500).send(errore);
          }
        }
      );
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
