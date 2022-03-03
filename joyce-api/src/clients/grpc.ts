import * as grpc from "@grpc/grpc-js";
import logFactory from "pino";

import { SchemaApiClient } from "@generated/grpc/api/schema_api_grpc_pb";
import { Schema } from "@generated/grpc/model/schema_pb";
import { GetSchemaRequest } from "@generated/grpc/api/schema_api_pb";
import { JoyceUriSubtype } from "@generated/grpc/enumeration/joyce_uri_subtype_pb";

import type { Schema as JsonSchema } from "@src/types";
const SCHEMA_GRPC_ENDPOINT = process.env.SCHEMA_GRPC_ENDPOINT || "import-gateway:6666";
const logger = logFactory({ name: "grpc-client" });
const client = new SchemaApiClient(SCHEMA_GRPC_ENDPOINT, grpc.credentials.createInsecure());

export function getSchema(uid: string): Promise<JsonSchema> {
	return new Promise((resolve, reject) => {
		const request = new GetSchemaRequest();
		request.setId(uid);
		logger.info(`getting schema ${uid}`);

		client.getSchema(request, (error, response) => {
			if (error) {
				reject(new Error(`cannot read grpc schema, cause: ${error.message}`));
			} else {
				if (response.hasSchema()) {
					resolve(toJsonSchema(response.getSchema()));
				} else {
					logger.debug(`no such schema ${uid}`);
					reject();
				}
			}
		});
	});
}

function toJsonSchema(protoSchema: Schema): JsonSchema {
	const protoObj = protoSchema.toObject();

	const schema: JsonSchema = {
		uid: protoSchema.getUid(),
		$schema: protoSchema.getSchema(),
		type: protoSchema.getType(),
		required: protoSchema.getRequiredList(),

		metadata: {
			...protoSchema.getMetadata().toObject(),
			uid: protoObj.uid,
			indexes: protoObj.metadata.indexesList.map((index) => Object.fromEntries(index.indexMap)),
			// non c'è bisogno di parsare gli extra
			// extra: JSON.parse(protoObj.metadata.extra || "null"),
			subtype: Object.keys(JoyceUriSubtype).find((subtype) => Number(JoyceUriSubtype[subtype]) === Number(protoObj.metadata.subtype)) as keyof typeof JoyceUriSubtype,
			development: protoObj.metadata.development === "true",
			store: protoObj.metadata.store === "true",
			validation: protoObj.metadata.validation === "true",
			indexed: protoObj.metadata.indexed === "true",
			connectors: protoObj.metadata.connectors === "true",
			pb_export: protoObj.metadata.pb_export === "true",
		},
		properties: protoSchema.getProperties().toJavaScript(),

	};

	(schema.metadata as any).indexesList = undefined;
	(schema as any).schema = undefined;

	return schema;
}
