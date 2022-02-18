// package: 
// file: model/mongo_index.proto

/* tslint:disable */
/* eslint-disable */

import * as jspb from "google-protobuf";

export class MongoIndex extends jspb.Message { 

    getIndexMap(): jspb.Map<string, number>;
    clearIndexMap(): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): MongoIndex.AsObject;
    static toObject(includeInstance: boolean, msg: MongoIndex): MongoIndex.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: MongoIndex, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): MongoIndex;
    static deserializeBinaryFromReader(message: MongoIndex, reader: jspb.BinaryReader): MongoIndex;
}

export namespace MongoIndex {
    export type AsObject = {

        indexMap: Array<[string, number]>,
    }
}
