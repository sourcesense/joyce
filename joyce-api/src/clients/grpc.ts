import * as grpc from "@grpc/grpc-js";
import logFactory from "pino";
import { SchemaApiClient } from "@generated/grpc/api/schema_api_grpc_pb";
import { Schema } from "@generated/grpc/model/schema_pb";
import * as google_protobuf_wrappers_pb from "google-protobuf/google/protobuf/wrappers_pb";

const logger = logFactory({ name: "grpc-client" });
const client = new SchemaApiClient("172.16.6.2:30744", grpc.credentials.createInsecure());

export function getSchema(uid: string): Promise<Schema> {
	return new Promise((resolve, reject) => {
		const request = new google_protobuf_wrappers_pb.StringValue();
		request.setValue(uid);

		client.getSchema(request, (error, response) => {
			if (error) {
				reject(new Error(`cannot read grpc schema, cause: ${error.message}`));
			} else {
				if (response.hasSchema()) {
					resolve(response.getSchema());
				} else {
					logger.debug(`no such schema ${uid}`);
					reject();
				}
			}
		});
	});
}
