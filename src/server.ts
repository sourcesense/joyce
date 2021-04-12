import fastify from "fastify";
import healthHandler from "./modules/health/routes";
import { CustomeSchemaParser } from "./plugins/CustomSchemaParser";
import fs from "fs";
import path from "path";
import {
  ResponsableSchema,
  SchemaConfiguration,
} from "./plugins/SchemaConfiguration";
const schemaSources = fs.readFileSync(
  path.join(__dirname, "../assets/schemas.json"),
  "utf8"
);
const schemaConfiguration = new SchemaConfiguration(JSON.parse(schemaSources));
const globaleQueryStringPagination = {
  page: { type: "integer" },
  size: { type: "integer" },
};

const requests = schemaConfiguration.requestSchemas();

function createServer(db) {
  return Promise.all(requests).then((schemasList) => {
    const server = fastify();
    server.register(require("fastify-cors"));
    server.register(require("fastify-oas"), {
      routePrefix: "/docs",
      exposeRoute: true,
      swagger: {
        info: {
          title: "product api",
          description: "api documentation",
          version: "0.1.0",
        },
        servers: [
          { url: "http://localhost:3000", description: "development" },
          {
            url: "https://<production-url>",
            description: "production",
          },
        ],
        schemes: ["http"],
        consumes: ["application/json"],
        produces: ["application/json"],
      },
    });
    schemasList.map((schema: ResponsableSchema) => {
      const tempSchema = new CustomeSchemaParser(
        schema
        // JSON.parse(fs.readFileSync(label, "utf8"))
      );
      server.get<{ Querystring: { page: number | null; size: number | null } }>(
        `/${tempSchema.collectionName}/${schema.version}`,
        {
          schema: {
            querystring: globaleQueryStringPagination,
            response: {
              200: {
                type: "array",
                items: {
                  type: "object",
                  properties: tempSchema.getSchemaProperties(),
                },
              },
            },
          },
        },
        async function (req, res) {
          const { page = 0, size = 10 } = req.query;
          try {
            const collection = db.collection(
              schema.schema.$metadata.collection
            );
            const docs = await collection
              .find({})
              .limit(size)
              .skip(page * size)
              .toArray();
            res.status(200).send(docs);
          } catch (errore) {
            res.status(500).send(errore);
          }
        }
      );
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
