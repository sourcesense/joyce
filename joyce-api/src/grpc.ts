// node_modules/.bin/ts-node src/grpc.ts

import * as grpc from "@grpc/grpc-js";
// import * as protoLoader from '@grpc/proto-loader';
import * as model_schema_pb from "./grpc/model/schema_pb";
import * as google_protobuf_empty_pb from "google-protobuf/google/protobuf/empty_pb";
import * as google_protobuf_wrappers_pb from "google-protobuf/google/protobuf/wrappers_pb";
import { SchemaApiClient } from "./grpc/api/schema_api_grpc_pb";
import { RequestParams } from "./grpc/api/schema_api_pb";
import { JoyceUriSubtype } from "./grpc/enumeration/joyce_uri_subtype_pb";

const logger = require("pino")();

// protoLoader.

// grpc.loadPackageDefinition(definition);

const credentials = grpc.credentials.createInsecure();
const asd = new SchemaApiClient("localhost:6666", credentials);

// -------------------

const schemaRequest: google_protobuf_wrappers_pb.StringValue = new google_protobuf_wrappers_pb.StringValue();
schemaRequest.setValue("joyce://schema/import/demo5.developer");

const call: grpc.ClientUnaryCall = asd.getSchema(new google_protobuf_wrappers_pb.StringValue(["joyce://schema/import/demo5.developer"]), (error: grpc.ServiceError, response: model_schema_pb.OptionalSchema) => {
	logger.info({ r: schemaRequest.toObject(), uid: new google_protobuf_wrappers_pb.StringValue(["joyce://schema/import/demo5.developer"]).toObject(), error, response }, "getting optional schema");
	if (response) {
		const optschema: model_schema_pb.OptionalSchema = response;
		logger.info({ xx: true, optschema: optschema.toObject() }, "this schema");
	}
});

// -------------------

const allRequest = new RequestParams();
allRequest.setNamespace("demo5");
allRequest.setSubtype(JoyceUriSubtype.IMPORT);
allRequest.setRootonly("true");

const all = asd.getAllSchemasBySubtypeAndNamespace(allRequest);
all.on("data", (data: google_protobuf_wrappers_pb.StringValue) => {
	logger.info({ schema: data.toObject() }, `[getAllSchemasBySubtypeAndNamespace] schema`);
});
all.on("error", (err) => {
	logger.warn(err);
});
all.on("end", () => {
	logger.info("[getAllSchemasBySubtypeAndNamespace] Done.");
});

// -------------------

const stream: grpc.ClientReadableStream<google_protobuf_wrappers_pb.StringValue> =asd.getAllNamespaces(new google_protobuf_empty_pb.Empty(), {});

stream.on("data", (data: google_protobuf_wrappers_pb.StringValue) => {
	logger.info(`[getAllNamespaces] Namespace: ${JSON.stringify(data.toObject())}`);
});
stream.on("end", () => {
	logger.info("[getAllNamespaces] Done.");
});

// -------------------

process.on("uncaughtException", (err) => {
	logger.warn(err, `process on uncaughtException error: ${err}`);
});
process.on("unhandledRejection", (err) => {
	logger.warn(err, `process on unhandledRejection error: ${err}`);
});
