// package: 
// file: model/schema.proto

/* tslint:disable */
/* eslint-disable */

import * as jspb from "google-protobuf";
import * as google_protobuf_struct_pb from "google-protobuf/google/protobuf/struct_pb";
import * as model_schema_metadata_pb from "../model/schema_metadata_pb";

export class Schema extends jspb.Message { 
    getUid(): string;
    setUid(value: string): Schema;
    getSchema(): string;
    setSchema(value: string): Schema;

    hasMetadata(): boolean;
    clearMetadata(): void;
    getMetadata(): model_schema_metadata_pb.SchemaMetadata | undefined;
    setMetadata(value?: model_schema_metadata_pb.SchemaMetadata): Schema;
    getType(): string;
    setType(value: string): Schema;
    clearRequiredList(): void;
    getRequiredList(): Array<string>;
    setRequiredList(value: Array<string>): Schema;
    addRequired(value: string, index?: number): string;
    getProperties(): string;
    setProperties(value: string): Schema;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Schema.AsObject;
    static toObject(includeInstance: boolean, msg: Schema): Schema.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Schema, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Schema;
    static deserializeBinaryFromReader(message: Schema, reader: jspb.BinaryReader): Schema;
}

export namespace Schema {
    export type AsObject = {
        uid: string,
        schema: string,
        metadata?: model_schema_metadata_pb.SchemaMetadata.AsObject,
        type: string,
        requiredList: Array<string>,
        properties: string,
    }
}

export class OptionalSchema extends jspb.Message { 

    hasSchema(): boolean;
    clearSchema(): void;
    getSchema(): Schema | undefined;
    setSchema(value?: Schema): OptionalSchema;

    hasNull(): boolean;
    clearNull(): void;
    getNull(): google_protobuf_struct_pb.NullValue;
    setNull(value: google_protobuf_struct_pb.NullValue): OptionalSchema;

    getKindCase(): OptionalSchema.KindCase;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): OptionalSchema.AsObject;
    static toObject(includeInstance: boolean, msg: OptionalSchema): OptionalSchema.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: OptionalSchema, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): OptionalSchema;
    static deserializeBinaryFromReader(message: OptionalSchema, reader: jspb.BinaryReader): OptionalSchema;
}

export namespace OptionalSchema {
    export type AsObject = {
        schema?: Schema.AsObject,
        pb_null: google_protobuf_struct_pb.NullValue,
    }

    export enum KindCase {
        KIND_NOT_SET = 0,
        SCHEMA = 1,
        NULL = 2,
    }

}
