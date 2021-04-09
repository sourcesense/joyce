import fastify from "fastify";
import healthHandler from "./modules/health/routes";
import collectionHandler from "./modules/products/routes";
import { CustomeSchemaParser } from "./plugins/CustomSchemaParser";
import fs from "fs";
import path from "path";
const schemaString = fs.readFileSync(
  path.join(__dirname, "../assets/user.json"),
  "utf8"
);
const schema = new CustomeSchemaParser(JSON.parse(schemaString));
console.log(schema);
schema.getSchemaProperties();
// const bodyJsonSchema = {
//   type: "object",
//   required: ["requiredKey"],
//   properties: {
//     someKey: { type: "string" },
//     someOtherKey: { type: "number" },
//     requiredKey: {
//       type: "array",
//       maxItems: 3,
//       items: { type: "integer" },
//     },
//     nullableKey: { type: ["number", "null"] },
//     multipleTypesKey: { type: ["boolean", "number"] },
//   },
// };

const bodyJsonSchema = {
  type: "object",
  required: schema.required,
  properties: schema.getSchemaProperties(),
};

const queryStringJsonSchema = {
  page: { type: "integer" },
  size: { type: "integer" },
};

const paramsJsonSchema = undefined;
// const paramsJsonSchema = {
//   type: "object",
//   properties: {
//     par1: { type: "string" },
//     par2: { type: "number" },
//   },
// };

const schemas = {
  body: bodyJsonSchema,

  querystring: queryStringJsonSchema,

  params: paramsJsonSchema,
};

function createServer(db) {
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

  server.get(
    `/${schema.collectionName}/v1`,
    {
      schema: {
        querystring: queryStringJsonSchema,
        response: {
          200: { type: "object", properties: schema.getSchemaProperties() },
        },
      },
    },
    function (req, res) {
      res.status(200).send({});
    }
  );

  server.get(
    `/${schema.collectionName}/v2`,
    {
      schema: {
        querystring: queryStringJsonSchema,
        response: {
          200: { type: "object", properties: schema.getSchemaProperties() },
        },
      },
    },
    function (req, res) {
      res.status(200).send({});
    }
  );
  // server.post(
  //   `/collections/${schema.collectionName}`,
  //   { schema: schemas },
  //   function (req, res) {
  //     res.status(200).send({});
  //   }
  // );

  server.register(healthHandler, { prefix: "/health" });
  server.register(collectionHandler(db), { prefix: "/collection" });

  server.setErrorHandler((error, req, res) => {
    req.log.error(error.toString());
    res.send({ error });
  });

  return server;
}

export default createServer;
