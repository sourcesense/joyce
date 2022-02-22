require("module-alias/register");

const { ApolloServer } = require("apollo-server-fastify");
const createServer = require("../server").default;
const KafkaProducerPromise = require("../plugins/KafkaClient").default;
const MongoClient = require("mongodb");

const PORT = process.env.PORT || "6650";
const mongoURI = process.env.MONGO_URI || "mongodb://localhost:27017/ingestion";

module.exports = async ({ getBuiltMesh, documents, logger }) => {

	const { schema, contextBuilder } = await getBuiltMesh();

	const apolloServer = new ApolloServer({
		schema,
		context: ({ req }) => contextBuilder(req),
		introspection: true,
		playground: true,
	});

	await apolloServer.start();
	const client = await MongoClient.connect(mongoURI, { useUnifiedTopology: true });

	const producerKafka = KafkaProducerPromise(logger);
	producerKafka.then(producer => {
		logger.info("kafka ready");
	});
	const server = await createServer(client.db(), producerKafka);

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
