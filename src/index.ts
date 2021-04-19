import createServer from "./server";
import MongoClient from "mongodb";
import { FastifyInstance } from "fastify";
const logger = require("pino")();
const PORT = process.env.PORT || "3000";
const mongoURI =
  process.env.MONGO_URI || "mongodb://user:password@localhost:27017/ingestion";

// Database Name
let server: FastifyInstance;
MongoClient.connect(mongoURI, function (err: Error, client) {
  if (err) {
    logger.error("Error connection Mongo", err);
  }

  logger.info("Connected successfully to Mongo");
  createServer(client.db()).then((server) => {
    process.on("SIGINT", function () {
      logger.info("Bye");
      client.close();
      process.exit();
    });
    server.listen(+PORT, "0.0.0.0", (err, address) => {
      if (err) throw err;
      server.log.info;
    });
  });
});

module.exports = server;
