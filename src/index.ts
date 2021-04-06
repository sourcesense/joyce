import createServer from "./server";
import MongoClient from "mongodb";
import { FastifyInstance } from "fastify";

const PORT = process.env.PORT || "3000";
const mongoURI =
  process.env.MONGO_URI ||
  "mongodb://my_user:password123@localhost:27017/my_database";

// Database Name
let server: FastifyInstance;
MongoClient.connect(mongoURI, function (err: Error, client) {
  if (err) {
    console.log("Error connection Mongo", err);
  }

  console.log("Connected successfully to Mongo");
  server = createServer(client.db());

  process.on("SIGINT", function () {
    console.log("Bye");
    client.close();
    process.exit();
  });
  server.listen(+PORT, "0.0.0.0", (err, address) => {
    if (err) throw err;
    console.log(`server listening on ${address}`);
  });
});

module.exports = server;
