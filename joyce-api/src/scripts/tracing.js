/* tracing.js */

// Require dependencies
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

const host = process.env.JAEGER_HOST;
const port = process.env.JAEGER_PORT;

if (host || port) {
	const provider = new NodeTracerProvider({
		resource: new Resource({
			[SemanticResourceAttributes.SERVICE_NAME]: "Joyce-API",
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
		host: process.env.JAEGER_HOST || "localhost",
		port: Number(process.env.JAEGER_PORT || 6832),
	};

	provider.addSpanProcessor(new BatchSpanProcessor(new JaegerExporter(jaegerOpts)));
}

