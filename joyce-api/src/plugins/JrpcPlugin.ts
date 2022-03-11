import { FastifyPluginCallback } from "fastify";
import { Kafka, logLevel as kafkaJsLogLevels, ProducerRecord } from "kafkajs";

const logger = require("pino")({ name: "jrpc-plugin" });
import { JRPCParams } from "@src/types";

const JOYCE_API_KAFKA_BOOTSTRAPADDRESS = process.env.JOYCE_API_KAFKA_BOOTSTRAPADDRESS || "kafka:9092";
const JOYCE_API_KAFKA_COMMAND_TOPIC = process.env.JOYCE_API_KAFKA_COMMAND_TOPIC || "joyce_command";
const clientId = process.env.JAEGER_SERVICE_NAME || "unknown_joyce_api:nodejs";

function toPinoLogLevel(kafkaLogLevel) {
	switch (kafkaLogLevel) {
		case kafkaJsLogLevels.ERROR:
			return "error";
		case kafkaJsLogLevels.WARN:
			return "warn";
		case kafkaJsLogLevels.DEBUG:
			return "debug";
		case kafkaJsLogLevels.NOTHING:
			return "silent";
		case kafkaJsLogLevels.INFO:
		default:
			return "info";
	}
}

function PinoLogCreator(logLevel) {
	const child = logger.child({
		name: "kafka-client",
		level: toPinoLogLevel(logLevel),
	});

	return ({ level, log }) => {
		const pinoLevel = toPinoLogLevel(level);
		const { message, ...extras } = log;
		child[pinoLevel](extras, message);
	};
}

const jrpcPlugin: FastifyPluginCallback<unknown> = async function (fastify, options, done) {
	logger.debug("Starting Json RPC Channel");

	const kafka = new Kafka({
		clientId,
		brokers: JOYCE_API_KAFKA_BOOTSTRAPADDRESS.split(/; ?/),
		logCreator: PinoLogCreator,
	});

	const producer = kafka.producer();

	const isConnected = new Promise((resolve, reject) => {
		producer.connect().then(() => {
			resolve(true);
		}).catch((error) => {
			producer.logger().warn("Kafka not available", error);
			reject(error);
			fastify.close().then(() => {
				logger.error("Server killed");
				process.exit();
			});
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
			const payload: ProducerRecord = {
				topic: JOYCE_API_KAFKA_COMMAND_TOPIC,
				messages: [{
					key: req.body.id,
					value: JSON.stringify(req.body)
				}],
			};
			isConnected.then(() => {
				producer.send(payload).then(() => {
					producer.logger().info("ok");
					res.status(200).send("OK");
					return "OK";
				}).catch((err) => {
					producer.logger().info("ko 1", err);
					res.status(500).send("KO");
					return "KO";
				});
			}).catch((e) => {
				logger.info("ko 2");
				logger.error(e);
				res.status(503).send("KO");
			});
		},
	);

	logger.info("Json RPC Channel started");
	done();
};

export default jrpcPlugin;
