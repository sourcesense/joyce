import fastify from "fastify";

import healthHandler from "./modules/health/routes";
import collectionHandler from "./modules/products/routes";

//require("./plugins/db");

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

  server.register(healthHandler, { prefix: "/health" });
  server.register(collectionHandler(db), { prefix: "/collection" });

  server.setErrorHandler((error, req, res) => {
    req.log.error(error.toString());
    res.send({ error });
  });

  return server;
}

export default createServer;
