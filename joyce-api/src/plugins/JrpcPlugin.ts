import { FastifyPluginCallback } from "fastify";
import { HighLevelProducer, KafkaClient } from "kafka-node";
const logger = require("pino")({ name: "jrpc-plugin" });

import { JRPCParams } from "@src/types";

const JOYCE_API_KAFKA_BOOTSTRAPADDRESS =
  process.env.JOYCE_API_KAFKA_BOOTSTRAPADDRESS || "kafka:9092";
const JOYCE_API_KAFKA_COMMAND_TOPIC = process.env.JOYCE_API_KAFKA_COMMAND_TOPIC || "joyce_command";

async function getProducer() {
	const client = new KafkaClient({
		kafkaHost: JOYCE_API_KAFKA_BOOTSTRAPADDRESS,
		autoConnect: true,
	});

	return new Promise<HighLevelProducer>((resolve, reject) => {
		const producer = new HighLevelProducer(client);

		producer.on("error", function (err) {
			return reject(err);
		});

		producer.on("ready", function () {
			logger.info(
				`Connected to Kafka @${JOYCE_API_KAFKA_BOOTSTRAPADDRESS}`
			);
			return resolve(producer);
		});
	});
}

const jrpcPlugin: FastifyPluginCallback<unknown> = async function (fastify, options, done) {

	logger.debug("Starting Json RPC Channel");

	const producer = getProducer().catch((error) => {
		logger.warn(error, "Kafka not available");
		fastify.close().then(() => {
			logger.error("Server killed");
			process.exit();
		});
	});

	fastify.post<{ Body: JRPCParams }>(
		"/jrpc",
		{
			schema: {
				tags: ["jrpc"],
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
					503: { type: "string", enum: ["KO"] },
				},
			},
		},
		function (req, res) {
			const payload = {
				topic: JOYCE_API_KAFKA_COMMAND_TOPIC,
				messages: JSON.stringify(req.body),
				key: req.body.id,
			};
			producer.then((p) => {
				if (p) {
					p.send([payload], function (err: any) {
						if (err) {
							logger.info(err, "ko 1");
							res.status(500).send("KO");
							return "KO";
						} else {
							logger.info("ok");
							res.status(200).send("OK");
							return "OK";
						}
					});
				} else {
					logger.info("ko 2");
					res.status(503).send("KO");
					return "KO";
				}
			}).catch((e) => {
				logger.info("ko 3");
				logger.error(e);
				res.status(503).send("KO");
			});
		},
	);

	logger.info("Json RPC Channel started");
	done();
};

export default jrpcPlugin;
