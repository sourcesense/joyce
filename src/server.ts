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
import { JRPCParams, ResponsableSchema } from "./types";
const logger = require("pino")();

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
const requests = schemaConfiguration.requestSchemas(logger);
const NILE_API_KAFKA_COMMAND_TOPIC =
  process.env.NILE_API_KAFKA_COMMAND_TOPIC || "commands";

// producer.on("ready", function (v) {
//   // payloads = [
//   //   { topic: "topic1", messages: "hi" },
//   //   { topic: "topic2", messages: ["hello", "world"] },
//   // ];
//   producer.send(
//     [
//       {
//         topic: "commands",
//         messages: "antani",
//       },
//     ],
//     (err, data) => {
//       if (err) {
//         console.log("error", err);
//       }
//       console.log("ok", data);
//     }
//   );
//   // producer.send(payloads, function (err, data) {
//   //     console.log(data);
//   // });
// });

function createServer(db, producer) {
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
    server.post<{ Body: JRPCParams }>(
      "/jrpc",
      {
        schema: {
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
        const params = req.body.params;
        // if (
        //   params &&
        //   Object.keys(params).length === 0 &&
        //   params.constructor === Object
        // ) {
        //   res.code(400).send({
        //     error: {
        //       validation: [
        //         {
        //           keyword: "required",
        //           dataPath: ".jsonrpc",
        //           schemaPath: "#/properties/jsonrpc/required",
        //           params: {
        //             missingProperty: "params",
        //           },
        //           message: "should not be empty",
        //         },
        //       ],
        //       validationContext: "body",
        //     },
        //   });
        // }
        const payload = {
          topic: NILE_API_KAFKA_COMMAND_TOPIC,
          messages: JSON.stringify(req.body),
          key: req.body.id,
        };
        producer.send([payload], function (err, data) {
          if (err) {
            res.status(500).send("KO");
            return;
          }
          res.status(200).send("OK");
          return;
        });
      }
    );
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
        `${tempSchema.development ? "/development" : ""}/${
          tempSchema.collectionName
        }/:id`,
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
        `${tempSchema.development ? "/development" : ""}/${
          tempSchema.collectionName
        }`,
        MultipleEntitySchema(tempSchema),
        async function (req, res) {
          const { page = 0, size = 10, orderBy, sortBy, ...other } = req.query;
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
