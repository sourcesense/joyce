// package: 
// file: api/schema_api.proto

/* tslint:disable */
/* eslint-disable */

import * as jspb from "google-protobuf";
import * as google_protobuf_empty_pb from "google-protobuf/google/protobuf/empty_pb";
import * as model_schema_pb from "../model/schema_pb";
import * as enumeration_joyce_uri_subtype_pb from "../enumeration/joyce_uri_subtype_pb";

export class GetSchemaRequest extends jspb.Message { 
    getId(): string;
    setId(value: string): GetSchemaRequest;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): GetSchemaRequest.AsObject;
    static toObject(includeInstance: boolean, msg: GetSchemaRequest): GetSchemaRequest.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: GetSchemaRequest, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): GetSchemaRequest;
    static deserializeBinaryFromReader(message: GetSchemaRequest, reader: jspb.BinaryReader): GetSchemaRequest;
}

export namespace GetSchemaRequest {
    export type AsObject = {
        id: string,
    }
}

export class GetAllSchemasRequest extends jspb.Message { 
    getRootonly(): string;
    setRootonly(value: string): GetAllSchemasRequest;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): GetAllSchemasRequest.AsObject;
    static toObject(includeInstance: boolean, msg: GetAllSchemasRequest): GetAllSchemasRequest.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: GetAllSchemasRequest, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): GetAllSchemasRequest;
    static deserializeBinaryFromReader(message: GetAllSchemasRequest, reader: jspb.BinaryReader): GetAllSchemasRequest;
}

export namespace GetAllSchemasRequest {
    export type AsObject = {
        rootonly: string,
    }
}

export class GetAllSchemasBySubtypeAndNamespaceRequest extends jspb.Message { 
    getSubtype(): enumeration_joyce_uri_subtype_pb.JoyceUriSubtype;
    setSubtype(value: enumeration_joyce_uri_subtype_pb.JoyceUriSubtype): GetAllSchemasBySubtypeAndNamespaceRequest;
    getNamespace(): string;
    setNamespace(value: string): GetAllSchemasBySubtypeAndNamespaceRequest;
    getRootonly(): string;
    setRootonly(value: string): GetAllSchemasBySubtypeAndNamespaceRequest;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): GetAllSchemasBySubtypeAndNamespaceRequest.AsObject;
    static toObject(includeInstance: boolean, msg: GetAllSchemasBySubtypeAndNamespaceRequest): GetAllSchemasBySubtypeAndNamespaceRequest.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: GetAllSchemasBySubtypeAndNamespaceRequest, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): GetAllSchemasBySubtypeAndNamespaceRequest;
    static deserializeBinaryFromReader(message: GetAllSchemasBySubtypeAndNamespaceRequest, reader: jspb.BinaryReader): GetAllSchemasBySubtypeAndNamespaceRequest;
}

export namespace GetAllSchemasBySubtypeAndNamespaceRequest {
    export type AsObject = {
        subtype: enumeration_joyce_uri_subtype_pb.JoyceUriSubtype,
        namespace: string,
        rootonly: string,
    }
}

export class GetSchemaResponse extends jspb.Message { 

    hasSchema(): boolean;
    clearSchema(): void;
    getSchema(): model_schema_pb.Schema | undefined;
    setSchema(value?: model_schema_pb.Schema): GetSchemaResponse;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): GetSchemaResponse.AsObject;
    static toObject(includeInstance: boolean, msg: GetSchemaResponse): GetSchemaResponse.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: GetSchemaResponse, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): GetSchemaResponse;
    static deserializeBinaryFromReader(message: GetSchemaResponse, reader: jspb.BinaryReader): GetSchemaResponse;
}

export namespace GetSchemaResponse {
    export type AsObject = {
        schema?: model_schema_pb.Schema.AsObject,
    }
}

export class GetSchemasResponse extends jspb.Message { 
    clearSchemasList(): void;
    getSchemasList(): Array<model_schema_pb.Schema>;
    setSchemasList(value: Array<model_schema_pb.Schema>): GetSchemasResponse;
    addSchemas(value?: model_schema_pb.Schema, index?: number): model_schema_pb.Schema;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): GetSchemasResponse.AsObject;
    static toObject(includeInstance: boolean, msg: GetSchemasResponse): GetSchemasResponse.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: GetSchemasResponse, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): GetSchemasResponse;
    static deserializeBinaryFromReader(message: GetSchemasResponse, reader: jspb.BinaryReader): GetSchemasResponse;
}

export namespace GetSchemasResponse {
    export type AsObject = {
        schemasList: Array<model_schema_pb.Schema.AsObject>,
    }
}

export class GetNamespacesResponse extends jspb.Message { 
    clearNamespacesList(): void;
    getNamespacesList(): Array<string>;
    setNamespacesList(value: Array<string>): GetNamespacesResponse;
    addNamespaces(value: string, index?: number): string;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): GetNamespacesResponse.AsObject;
    static toObject(includeInstance: boolean, msg: GetNamespacesResponse): GetNamespacesResponse.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: GetNamespacesResponse, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): GetNamespacesResponse;
    static deserializeBinaryFromReader(message: GetNamespacesResponse, reader: jspb.BinaryReader): GetNamespacesResponse;
}

export namespace GetNamespacesResponse {
    export type AsObject = {
        namespacesList: Array<string>,
    }
}
