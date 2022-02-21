// package: 
// file: api/schema_api.proto

/* tslint:disable */
/* eslint-disable */

import * as grpc from "@grpc/grpc-js";
import * as api_schema_api_pb from "../api/schema_api_pb";
import * as google_protobuf_empty_pb from "google-protobuf/google/protobuf/empty_pb";
import * as model_schema_pb from "../model/schema_pb";
import * as enumeration_joyce_uri_subtype_pb from "../enumeration/joyce_uri_subtype_pb";

interface ISchemaApiService extends grpc.ServiceDefinition<grpc.UntypedServiceImplementation> {
    getSchema: ISchemaApiService_IGetSchema;
    getAllSchemas: ISchemaApiService_IGetAllSchemas;
    getAllSchemasBySubtypeAndNamespace: ISchemaApiService_IGetAllSchemasBySubtypeAndNamespace;
    getAllSchemasByReportsIsNotEmpty: ISchemaApiService_IGetAllSchemasByReportsIsNotEmpty;
    getAllNamespaces: ISchemaApiService_IGetAllNamespaces;
}

interface ISchemaApiService_IGetSchema extends grpc.MethodDefinition<api_schema_api_pb.GetSchemaRequest, api_schema_api_pb.GetSchemaResponse> {
    path: "/SchemaApi/GetSchema";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<api_schema_api_pb.GetSchemaRequest>;
    requestDeserialize: grpc.deserialize<api_schema_api_pb.GetSchemaRequest>;
    responseSerialize: grpc.serialize<api_schema_api_pb.GetSchemaResponse>;
    responseDeserialize: grpc.deserialize<api_schema_api_pb.GetSchemaResponse>;
}
interface ISchemaApiService_IGetAllSchemas extends grpc.MethodDefinition<api_schema_api_pb.GetAllSchemasRequest, api_schema_api_pb.GetSchemasResponse> {
    path: "/SchemaApi/GetAllSchemas";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<api_schema_api_pb.GetAllSchemasRequest>;
    requestDeserialize: grpc.deserialize<api_schema_api_pb.GetAllSchemasRequest>;
    responseSerialize: grpc.serialize<api_schema_api_pb.GetSchemasResponse>;
    responseDeserialize: grpc.deserialize<api_schema_api_pb.GetSchemasResponse>;
}
interface ISchemaApiService_IGetAllSchemasBySubtypeAndNamespace extends grpc.MethodDefinition<api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, api_schema_api_pb.GetSchemasResponse> {
    path: "/SchemaApi/GetAllSchemasBySubtypeAndNamespace";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest>;
    requestDeserialize: grpc.deserialize<api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest>;
    responseSerialize: grpc.serialize<api_schema_api_pb.GetSchemasResponse>;
    responseDeserialize: grpc.deserialize<api_schema_api_pb.GetSchemasResponse>;
}
interface ISchemaApiService_IGetAllSchemasByReportsIsNotEmpty extends grpc.MethodDefinition<google_protobuf_empty_pb.Empty, api_schema_api_pb.GetSchemasResponse> {
    path: "/SchemaApi/GetAllSchemasByReportsIsNotEmpty";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<google_protobuf_empty_pb.Empty>;
    requestDeserialize: grpc.deserialize<google_protobuf_empty_pb.Empty>;
    responseSerialize: grpc.serialize<api_schema_api_pb.GetSchemasResponse>;
    responseDeserialize: grpc.deserialize<api_schema_api_pb.GetSchemasResponse>;
}
interface ISchemaApiService_IGetAllNamespaces extends grpc.MethodDefinition<google_protobuf_empty_pb.Empty, api_schema_api_pb.GetNamespacesResponse> {
    path: "/SchemaApi/GetAllNamespaces";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<google_protobuf_empty_pb.Empty>;
    requestDeserialize: grpc.deserialize<google_protobuf_empty_pb.Empty>;
    responseSerialize: grpc.serialize<api_schema_api_pb.GetNamespacesResponse>;
    responseDeserialize: grpc.deserialize<api_schema_api_pb.GetNamespacesResponse>;
}

export const SchemaApiService: ISchemaApiService;

export interface ISchemaApiServer extends grpc.UntypedServiceImplementation {
    getSchema: grpc.handleUnaryCall<api_schema_api_pb.GetSchemaRequest, api_schema_api_pb.GetSchemaResponse>;
    getAllSchemas: grpc.handleUnaryCall<api_schema_api_pb.GetAllSchemasRequest, api_schema_api_pb.GetSchemasResponse>;
    getAllSchemasBySubtypeAndNamespace: grpc.handleUnaryCall<api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, api_schema_api_pb.GetSchemasResponse>;
    getAllSchemasByReportsIsNotEmpty: grpc.handleUnaryCall<google_protobuf_empty_pb.Empty, api_schema_api_pb.GetSchemasResponse>;
    getAllNamespaces: grpc.handleUnaryCall<google_protobuf_empty_pb.Empty, api_schema_api_pb.GetNamespacesResponse>;
}

export interface ISchemaApiClient {
    getSchema(request: api_schema_api_pb.GetSchemaRequest, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemaResponse) => void): grpc.ClientUnaryCall;
    getSchema(request: api_schema_api_pb.GetSchemaRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemaResponse) => void): grpc.ClientUnaryCall;
    getSchema(request: api_schema_api_pb.GetSchemaRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemaResponse) => void): grpc.ClientUnaryCall;
    getAllSchemas(request: api_schema_api_pb.GetAllSchemasRequest, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemas(request: api_schema_api_pb.GetAllSchemasRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemas(request: api_schema_api_pb.GetAllSchemasRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    getAllNamespaces(request: google_protobuf_empty_pb.Empty, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetNamespacesResponse) => void): grpc.ClientUnaryCall;
    getAllNamespaces(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetNamespacesResponse) => void): grpc.ClientUnaryCall;
    getAllNamespaces(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetNamespacesResponse) => void): grpc.ClientUnaryCall;
}

export class SchemaApiClient extends grpc.Client implements ISchemaApiClient {
    constructor(address: string, credentials: grpc.ChannelCredentials, options?: Partial<grpc.ClientOptions>);
    public getSchema(request: api_schema_api_pb.GetSchemaRequest, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemaResponse) => void): grpc.ClientUnaryCall;
    public getSchema(request: api_schema_api_pb.GetSchemaRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemaResponse) => void): grpc.ClientUnaryCall;
    public getSchema(request: api_schema_api_pb.GetSchemaRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemaResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemas(request: api_schema_api_pb.GetAllSchemasRequest, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemas(request: api_schema_api_pb.GetAllSchemasRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemas(request: api_schema_api_pb.GetAllSchemasRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemasBySubtypeAndNamespace(request: api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllSchemasByReportsIsNotEmpty(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetSchemasResponse) => void): grpc.ClientUnaryCall;
    public getAllNamespaces(request: google_protobuf_empty_pb.Empty, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetNamespacesResponse) => void): grpc.ClientUnaryCall;
    public getAllNamespaces(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetNamespacesResponse) => void): grpc.ClientUnaryCall;
    public getAllNamespaces(request: google_protobuf_empty_pb.Empty, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: api_schema_api_pb.GetNamespacesResponse) => void): grpc.ClientUnaryCall;
}
