import createServer from "./server";
import MongoClient from "mongodb";
import { FastifyInstance } from "fastify";
import KafkaProducerPromise from "./plugins/KafkaClient";
const logger = require("pino")();
const PORT = process.env.PORT || "3000";
const mongoURI =
  process.env.MONGO_URI || "mongodb://user:password@localhost:27017/ingestion";

// Database Name
const producerKafka = KafkaProducerPromise(logger);
let server: FastifyInstance;
MongoClient.connect(
  mongoURI,
  { useUnifiedTopology: true },
  async function (err: Error, client) {
    if (err) {
      logger.error("Error connection Mongo", err);
    }

    logger.info("Connected successfully to Mongo");
    producerKafka
      .then((producer) => {
        return createServer(client.db(), producer);
      })
      .then((server) => {
        process.on("SIGINT", function () {
          logger.info("Bye");
          client.close();
          process.exit();
        });
        server.listen(+PORT, "0.0.0.0", (err, address) => {
          if (err) throw err;
          server.log.info;
        });
      })
      .catch((err) => logger.error(err));
  }
);
