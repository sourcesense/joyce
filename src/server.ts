import fastify from "fastify";
import healthHandler from "./modules/health/routes";
import collectionHandler from "./modules/products/routes";
import { CustomeSchemaParser } from "./plugins/CustomSchemaParser";
import fs from "fs";
import path from "path";
import { readeFolder, readeFolderPromise } from "./plugins/FolderReader";
// const schemaString = fs.readFileSync(
//   path.join(__dirname, "../schemas/user.json"),
//   "utf8"
// );
const schemasList_sync = readeFolder(path.join(__dirname, "../schemas"))
  .map((fls) => fs.readFileSync(fls))
  .reduce((acc, scm) => {
    const schemaParsed = new CustomeSchemaParser(JSON.parse(scm));
    return { ...acc, [schemaParsed.collectionName]: schemaParsed };
  }, {});

const globaleQueryStringPagination = {
  page: { type: "integer" },
  size: { type: "integer" },
};

//const paramsJsonSchema = undefined;
// const paramsJsonSchema = {
//   type: "object",
//   properties: {
//     par1: { type: "string" },
//     par2: { type: "number" },
//   },
// };

// const schemas = {
//   body: schemaString,

//   querystring: globaleQueryStringPagination,

//   params: paramsJsonSchema,
// };

function createServer(db) {
  return readeFolderPromise(path.join(__dirname, "../schemas")).then(
    (schemasList) => {
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
      schemasList.map((label) => {
        const tempSchema = new CustomeSchemaParser(
          JSON.parse(fs.readFileSync(label, "utf8"))
        );
        server.get(
          `/${tempSchema.collectionName}`,
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
          function (req, res) {
            res.status(200).send({});
          }
        );
      });

      // server.post(
      //   `/collections/${schema.collectionName}`,
      //   { schema: schemas },
      //   function (req, res) {
      //     res.status(200).send({});
      //   }
      // );

      server.register(healthHandler, { prefix: "/health" });

      server.setErrorHandler((error, req, res) => {
        req.log.error(error.toString());
        res.send({ error });
      });
      return server;
    }
  );
}
export default createServer;
