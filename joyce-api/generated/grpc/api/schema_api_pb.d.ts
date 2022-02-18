// package: 
// file: api/schema_api.proto

/* tslint:disable */
/* eslint-disable */

import * as jspb from "google-protobuf";
import * as google_protobuf_empty_pb from "google-protobuf/google/protobuf/empty_pb";
import * as google_protobuf_wrappers_pb from "google-protobuf/google/protobuf/wrappers_pb";
import * as model_schema_pb from "../model/schema_pb";
import * as enumeration_joyce_uri_subtype_pb from "../enumeration/joyce_uri_subtype_pb";

export class RequestParams extends jspb.Message { 
    getSubtype(): enumeration_joyce_uri_subtype_pb.JoyceUriSubtype;
    setSubtype(value: enumeration_joyce_uri_subtype_pb.JoyceUriSubtype): RequestParams;
    getNamespace(): string;
    setNamespace(value: string): RequestParams;
    getRootonly(): string;
    setRootonly(value: string): RequestParams;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): RequestParams.AsObject;
    static toObject(includeInstance: boolean, msg: RequestParams): RequestParams.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: RequestParams, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): RequestParams;
    static deserializeBinaryFromReader(message: RequestParams, reader: jspb.BinaryReader): RequestParams;
}

export namespace RequestParams {
    export type AsObject = {
        subtype: enumeration_joyce_uri_subtype_pb.JoyceUriSubtype,
        namespace: string,
        rootonly: string,
    }
}
