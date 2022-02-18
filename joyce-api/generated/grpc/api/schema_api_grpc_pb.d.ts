// package: 
// file: api/schema_api.proto

/* tslint:disable */
/* eslint-disable */

import * as grpc from "@grpc/grpc-js";
import * as api_schema_api_pb from "../api/schema_api_pb";
import * as google_protobuf_empty_pb from "google-protobuf/google/protobuf/empty_pb";
import * as google_protobuf_wrappers_pb from "google-protobuf/google/protobuf/wrappers_pb";
import * as model_schema_pb from "../model/schema_pb";
import * as enumeration_joyce_uri_subtype_pb from "../enumeration/joyce_uri_subtype_pb";

interface ISchemaApiService extends grpc.ServiceDefinition<grpc.UntypedServiceImplementation> {
    getSchema: ISchemaApiService_IGetSchema;
    getAllSchemas: ISchemaApiService_IGetAllSchemas;
    getAllSchemasBySubtypeAndNamespace: ISchemaApiService_IGetAllSchemasBySubtypeAndNamespace;
    getAllSchemasByReportsIsNotEmpty: ISchemaApiService_IGetAllSchemasByReportsIsNotEmpty;
    getAllNamespaces: ISchemaApiService_IGetAllNamespaces;
}

interface ISchemaApiService_IGetSchema extends grpc.MethodDefinition<google_protobuf_wrappers_pb.StringValue, model_schema_pb.OptionalSchema> {
    path: "/SchemaApi/GetSchema";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<google_protobuf_wrappers_pb.StringValue>;
    requestDeserialize: grpc.deserialize<google_protobuf_wrappers_pb.StringValue>;
    responseSerialize: grpc.serialize<model_schema_pb.OptionalSchema>;
    responseDeserialize: grpc.deserialize<model_schema_pb.OptionalSchema>;
}
interface ISchemaApiService_IGetAllSchemas extends grpc.MethodDefinition<google_protobuf_wrappers_pb.StringValue, model_schema_pb.Schema> {
    path: "/SchemaApi/GetAllSchemas";
    requestStream: false;
    responseStream: true;
    requestSerialize: grpc.serialize<google_protobuf_wrappers_pb.StringValue>;
    requestDeserialize: grpc.deserialize<google_protobuf_wrappers_pb.StringValue>;
    responseSerialize: grpc.serialize<model_schema_pb.Schema>;
    responseDeserialize: grpc.deserialize<model_schema_pb.Schema>;
}
interface ISchemaApiService_IGetAllSchemasBySubtypeAndNamespace extends grpc.MethodDefinition<api_schema_api_pb.RequestParams, model_schema_pb.Schema> {
    path: "/SchemaApi/GetAllSchemasBySubtypeAndNamespace";
    requestStream: false;
    responseStream: true;
    requestSerialize: grpc.serialize<api_schema_api_pb.RequestParams>;
    requestDeserialize: grpc.deserialize<api_schema_api_pb.RequestParams>;
    responseSerialize: grpc.serialize<model_schema_pb.Schema>;
    responseDeserialize: grpc.deserialize<model_schema_pb.Schema>;
}
interface ISchemaApiService_IGetAllSchemasByReportsIsNotEmpty extends grpc.MethodDefinition<google_protobuf_empty_pb.Empty, model_schema_pb.Schema> {
    path: "/SchemaApi/GetAllSchemasByReportsIsNotEmpty";
    requestStream: false;
    responseStream: true;
    requestSerialize: grpc.serialize<google_protobuf_empty_pb.Empty>;
    requestDeserialize: grpc.deserialize<google_protobuf_empty_pb.Empty>;
    responseSerialize: grpc.serialize<model_schema_pb.Schema>;
    responseDeserialize: grpc.deserialize<model_schema_pb.Schema>;
}
interface ISchemaApiService_IGetAllNamespaces extends grpc.MethodDefinition<google_protobuf_empty_pb.Empty, google_protobuf_wrappers_pb.StringValue> {
    path: "/SchemaApi/GetAllNamespaces";
    requestStream: false;
    responseStream: true;
    requestSerialize: grpc.serialize<google_protobuf_empty_pb.Empty>;
    requestDeserialize: grpc.deserialize<google_protobuf_empty_pb.Empty>;
    responseSerialize: grpc.serialize<google_protobuf_wrappers_pb.StringValue>;
    responseDeserialize: grpc.deserialize<google_protobuf_wrappers_pb.StringValue>;
}

export const SchemaApiService: ISchemaApiService;

export interface ISchemaApiServer extends grpc.UntypedServiceImplementation {
    getSchema: grpc.handleUnaryCall<google_protobuf_wrappers_pb.StringValue, model_schema_pb.OptionalSchema>;
    getAllSchemas: grpc.handleServerStreamingCall<google_protobuf_wrappers_pb.StringValue, model_schema_pb.Schema>;
    getAllSchemasBySubtypeAndNamespace: grpc.handleServerStreamingCall<api_schema_api_pb.RequestParams, model_schema_pb.Schema>;
    getAllSchemasByReportsIsNotEmpty: grpc.handleServerStreamingCall<google_protobuf_empty_pb.Empty, model_schema_pb.Schema>;
    getAllNamespaces: grpc.handleServerStreamingCall<google_protobuf_empty_pb.Empty, google_protobuf_wrappers_pb.StringValue>;
}

export interface ISchemaApiClient {
    getSchema(request: google_protobuf_wrappers_pb.StringValue, callback: (error: grpc.ServiceError | null, response: model_schema_pb.OptionalSchema) => void): grpc.ClientUnaryCall;
    getSchema(request: google_protobuf_wrappers_pb.StringValue, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: model_schema_pb.OptionalSchema) => void): grpc.ClientUnaryCall;
    getSchema(request: google_protobuf_wrappers_pb.StringValue, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: model_schema_pb.OptionalSchema) => void): grpc.ClientUnaryCall;
    getAllSchemas(request: google_protobuf_wrappers_pb.StringValue, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    getAllSchemas(request: google_protobuf_wrappers_pb.StringValue, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.RequestParams, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.RequestParams, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    getAllNamespaces(request: google_protobuf_empty_pb.Empty, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<google_protobuf_wrappers_pb.StringValue>;
    getAllNamespaces(request: google_protobuf_empty_pb.Empty, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<google_protobuf_wrappers_pb.StringValue>;
}

export class SchemaApiClient extends grpc.Client implements ISchemaApiClient {
    constructor(address: string, credentials: grpc.ChannelCredentials, options?: Partial<grpc.ClientOptions>);
    public getSchema(request: google_protobuf_wrappers_pb.StringValue, callback: (error: grpc.ServiceError | null, response: model_schema_pb.OptionalSchema) => void): grpc.ClientUnaryCall;
    public getSchema(request: google_protobuf_wrappers_pb.StringValue, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: model_schema_pb.OptionalSchema) => void): grpc.ClientUnaryCall;
    public getSchema(request: google_protobuf_wrappers_pb.StringValue, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: model_schema_pb.OptionalSchema) => void): grpc.ClientUnaryCall;
    public getAllSchemas(request: google_protobuf_wrappers_pb.StringValue, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    public getAllSchemas(request: google_protobuf_wrappers_pb.StringValue, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    public getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.RequestParams, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    public getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.RequestParams, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    public getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    public getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<model_schema_pb.Schema>;
    public getAllNamespaces(request: google_protobuf_empty_pb.Empty, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<google_protobuf_wrappers_pb.StringValue>;
    public getAllNamespaces(request: google_protobuf_empty_pb.Empty, metadata?: grpc.Metadata, options?: Partial<grpc.CallOptions>): grpc.ClientReadableStream<google_protobuf_wrappers_pb.StringValue>;
}
