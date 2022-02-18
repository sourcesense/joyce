// package: 
// file: model/schema_metadata.proto

/* tslint:disable */
/* eslint-disable */

import * as jspb from "google-protobuf";
import * as enumeration_joyce_uri_subtype_pb from "../enumeration/joyce_uri_subtype_pb";
import * as model_mongo_index_pb from "../model/mongo_index_pb";

export class SchemaMetadata extends jspb.Message { 
    getUidkey(): string;
    setUidkey(value: string): SchemaMetadata;
    getSubtype(): enumeration_joyce_uri_subtype_pb.JoyceUriSubtype;
    setSubtype(value: enumeration_joyce_uri_subtype_pb.JoyceUriSubtype): SchemaMetadata;
    getCollection(): string;
    setCollection(value: string): SchemaMetadata;
    getName(): string;
    setName(value: string): SchemaMetadata;
    getNamespace(): string;
    setNamespace(value: string): SchemaMetadata;
    getDescription(): string;
    setDescription(value: string): SchemaMetadata;
    getParent(): string;
    setParent(value: string): SchemaMetadata;
    clearIndexesList(): void;
    getIndexesList(): Array<model_mongo_index_pb.MongoIndex>;
    setIndexesList(value: Array<model_mongo_index_pb.MongoIndex>): SchemaMetadata;
    addIndexes(value?: model_mongo_index_pb.MongoIndex, index?: number): model_mongo_index_pb.MongoIndex;
    getDevelopment(): string;
    setDevelopment(value: string): SchemaMetadata;
    getStore(): string;
    setStore(value: string): SchemaMetadata;
    getValidation(): string;
    setValidation(value: string): SchemaMetadata;
    getIndexed(): string;
    setIndexed(value: string): SchemaMetadata;
    getConnectors(): string;
    setConnectors(value: string): SchemaMetadata;
    getExport(): string;
    setExport(value: string): SchemaMetadata;
    getExtra(): string;
    setExtra(value: string): SchemaMetadata;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): SchemaMetadata.AsObject;
    static toObject(includeInstance: boolean, msg: SchemaMetadata): SchemaMetadata.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: SchemaMetadata, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): SchemaMetadata;
    static deserializeBinaryFromReader(message: SchemaMetadata, reader: jspb.BinaryReader): SchemaMetadata;
}

export namespace SchemaMetadata {
    export type AsObject = {
        uidkey: string,
        subtype: enumeration_joyce_uri_subtype_pb.JoyceUriSubtype,
        collection: string,
        name: string,
        namespace: string,
        description: string,
        parent: string,
        indexesList: Array<model_mongo_index_pb.MongoIndex.AsObject>,
        development: string,
        store: string,
        validation: string,
        indexed: string,
        connectors: string,
        pb_export: string,
        extra: string,
    }
}
