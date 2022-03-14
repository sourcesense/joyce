const { NodeTracerProvider } = require("@opentelemetry/node");
const { registerInstrumentations } = require("@opentelemetry/instrumentation");
const { HttpInstrumentation } = require("@opentelemetry/instrumentation-http");
const { FastifyInstrumentation } = require("@opentelemetry/instrumentation-fastify");
const { Resource } = require("@opentelemetry/resources");
const { SemanticResourceAttributes } = require("@opentelemetry/semantic-conventions");
const { GraphQLInstrumentation } = require("@opentelemetry/instrumentation-graphql");
const { JaegerExporter } = require("@opentelemetry/exporter-jaeger");
const { BatchSpanProcessor } = require("@opentelemetry/sdk-trace-base");
const { MongooseInstrumentation } = require("opentelemetry-instrumentation-mongoose");
const { MongoDBInstrumentation } = require("@opentelemetry/instrumentation-mongodb");

const logger = require("pino")({ name: "tracing", level: "debug" });

const enabled = process.env.JOYCE_API_ENABLE_TRACING === "true";

if (!enabled) {
	logger.debug("tracing not enabled");
	return;
}
logger.debug("tracing enabled");

const host = process.env.JAEGER_HOST;

if (host) {
	const provider = new NodeTracerProvider({
		resource: new Resource({
			[SemanticResourceAttributes.SERVICE_NAME]: process.env.JAEGER_SERVICE_NAME || "unknown_service:nodejs",
			[SemanticResourceAttributes.SERVICE_NAMESPACE]: process.env.JAEGER_SERVICE_NAMESPACE || "Joyce.API"
		}),
	});
	provider.register({});

	registerInstrumentations({
		instrumentations: [
			new HttpInstrumentation(),
			new MongoDBInstrumentation(),
			new GraphQLInstrumentation(),
			new MongooseInstrumentation(),
			new FastifyInstrumentation(),
		],
	});

	const jaegerOpts = {
		host: process.env.JAEGER_HOST,
		port: Number(process.env.JAEGER_PORT || 6832),
	};

	provider.addSpanProcessor(new BatchSpanProcessor(new JaegerExporter(jaegerOpts)));
} else {
	logger.warn("opentelemetry needs JAEGER_HOST env variable");
}

