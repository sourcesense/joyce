// GENERATED CODE -- DO NOT EDIT!

'use strict';
var grpc = require('@grpc/grpc-js');
var api_schema_api_pb = require('../api/schema_api_pb.js');
var google_protobuf_empty_pb = require('google-protobuf/google/protobuf/empty_pb.js');
var model_schema_pb = require('../model/schema_pb.js');
var enumeration_joyce_uri_subtype_pb = require('../enumeration/joyce_uri_subtype_pb.js');

function serialize_GetAllSchemasBySubtypeAndNamespaceRequest(arg) {
  if (!(arg instanceof api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest)) {
    throw new Error('Expected argument of type GetAllSchemasBySubtypeAndNamespaceRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_GetAllSchemasBySubtypeAndNamespaceRequest(buffer_arg) {
  return api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_GetAllSchemasRequest(arg) {
  if (!(arg instanceof api_schema_api_pb.GetAllSchemasRequest)) {
    throw new Error('Expected argument of type GetAllSchemasRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_GetAllSchemasRequest(buffer_arg) {
  return api_schema_api_pb.GetAllSchemasRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_GetNamespacesResponse(arg) {
  if (!(arg instanceof api_schema_api_pb.GetNamespacesResponse)) {
    throw new Error('Expected argument of type GetNamespacesResponse');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_GetNamespacesResponse(buffer_arg) {
  return api_schema_api_pb.GetNamespacesResponse.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_GetSchemaRequest(arg) {
  if (!(arg instanceof api_schema_api_pb.GetSchemaRequest)) {
    throw new Error('Expected argument of type GetSchemaRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_GetSchemaRequest(buffer_arg) {
  return api_schema_api_pb.GetSchemaRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_GetSchemaResponse(arg) {
  if (!(arg instanceof api_schema_api_pb.GetSchemaResponse)) {
    throw new Error('Expected argument of type GetSchemaResponse');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_GetSchemaResponse(buffer_arg) {
  return api_schema_api_pb.GetSchemaResponse.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_GetSchemasResponse(arg) {
  if (!(arg instanceof api_schema_api_pb.GetSchemasResponse)) {
    throw new Error('Expected argument of type GetSchemasResponse');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_GetSchemasResponse(buffer_arg) {
  return api_schema_api_pb.GetSchemasResponse.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_google_protobuf_Empty(arg) {
  if (!(arg instanceof google_protobuf_empty_pb.Empty)) {
    throw new Error('Expected argument of type google.protobuf.Empty');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_google_protobuf_Empty(buffer_arg) {
  return google_protobuf_empty_pb.Empty.deserializeBinary(new Uint8Array(buffer_arg));
}


var SchemaApiService = exports.SchemaApiService = {
  getSchema: {
    path: '/SchemaApi/GetSchema',
    requestStream: false,
    responseStream: false,
    requestType: api_schema_api_pb.GetSchemaRequest,
    responseType: api_schema_api_pb.GetSchemaResponse,
    requestSerialize: serialize_GetSchemaRequest,
    requestDeserialize: deserialize_GetSchemaRequest,
    responseSerialize: serialize_GetSchemaResponse,
    responseDeserialize: deserialize_GetSchemaResponse,
  },
  getAllSchemas: {
    path: '/SchemaApi/GetAllSchemas',
    requestStream: false,
    responseStream: false,
    requestType: api_schema_api_pb.GetAllSchemasRequest,
    responseType: api_schema_api_pb.GetSchemasResponse,
    requestSerialize: serialize_GetAllSchemasRequest,
    requestDeserialize: deserialize_GetAllSchemasRequest,
    responseSerialize: serialize_GetSchemasResponse,
    responseDeserialize: deserialize_GetSchemasResponse,
  },
  getAllSchemasBySubtypeAndNamespace: {
    path: '/SchemaApi/GetAllSchemasBySubtypeAndNamespace',
    requestStream: false,
    responseStream: false,
    requestType: api_schema_api_pb.GetAllSchemasBySubtypeAndNamespaceRequest,
    responseType: api_schema_api_pb.GetSchemasResponse,
    requestSerialize: serialize_GetAllSchemasBySubtypeAndNamespaceRequest,
    requestDeserialize: deserialize_GetAllSchemasBySubtypeAndNamespaceRequest,
    responseSerialize: serialize_GetSchemasResponse,
    responseDeserialize: deserialize_GetSchemasResponse,
  },
  getAllSchemasByReportsIsNotEmpty: {
    path: '/SchemaApi/GetAllSchemasByReportsIsNotEmpty',
    requestStream: false,
    responseStream: false,
    requestType: google_protobuf_empty_pb.Empty,
    responseType: api_schema_api_pb.GetSchemasResponse,
    requestSerialize: serialize_google_protobuf_Empty,
    requestDeserialize: deserialize_google_protobuf_Empty,
    responseSerialize: serialize_GetSchemasResponse,
    responseDeserialize: deserialize_GetSchemasResponse,
  },
  getAllNamespaces: {
    path: '/SchemaApi/GetAllNamespaces',
    requestStream: false,
    responseStream: false,
    requestType: google_protobuf_empty_pb.Empty,
    responseType: api_schema_api_pb.GetNamespacesResponse,
    requestSerialize: serialize_google_protobuf_Empty,
    requestDeserialize: deserialize_google_protobuf_Empty,
    responseSerialize: serialize_GetNamespacesResponse,
    responseDeserialize: deserialize_GetNamespacesResponse,
  },
};

exports.SchemaApiClient = grpc.makeGenericClientConstructor(SchemaApiService);
