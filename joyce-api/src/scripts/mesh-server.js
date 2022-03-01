require("module-alias/register");

const { readConfig } = require("@src/utils/config-util");
const { ApolloServer } = require("apollo-server-fastify");
const createServer = require("../server").default;
// const KafkaProducerPromise = require("../plugins/KafkaClient").default;
const MongoClient = require("mongodb");

const PORT = process.env.PORT || "6650";
const mongoURI = process.env.MONGO_URI || "mongodb://localhost:27017/ingestion";
const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "src/templates/schemas.json";

module.exports = async ({ getBuiltMesh, documents, logger }) => {
	const client = await MongoClient.connect(mongoURI, { useUnifiedTopology: true });

	client.on("error", function (error) {
		logger.error(error, "Error in MongoDb connection");
	});

	const config = await readConfig(SCHEMAS_SOURCE);

	const server = await createServer(client.db());

	const { schema, contextBuilder } = await getBuiltMesh();

	const apolloServer = new ApolloServer({
		schema,
		context: ({ req }) => contextBuilder(req),
		introspection: true,
		playground: true,
	});

	await apolloServer.start();
	// const producerKafka = KafkaProducerPromise(logger);
	// producerKafka.then(producer => {
	// 	logger.info("kafka ready");
	// });
	// const server = await createServer(client.db(), producerKafka);

	server.register(apolloServer.createHandler());
	server.get("/graphiql", (_, reply) => {
		reply.sendFile("graphiql.html");
	});

	process.on("SIGINT", function () {
		logger.info("Bye");
		client.close();
		process.exit();
	});
	server.listen(+PORT, "0.0.0.0", (err) => {
		if (err) {
			throw err;
		}
		server.log.info;
	});
	return { apolloServer, server };
};
