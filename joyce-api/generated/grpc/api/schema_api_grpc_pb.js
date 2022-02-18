// GENERATED CODE -- DO NOT EDIT!

'use strict';
var grpc = require('@grpc/grpc-js');
var api_schema_api_pb = require('../api/schema_api_pb.js');
var google_protobuf_empty_pb = require('google-protobuf/google/protobuf/empty_pb.js');
var google_protobuf_wrappers_pb = require('google-protobuf/google/protobuf/wrappers_pb.js');
var model_schema_pb = require('../model/schema_pb.js');
var enumeration_joyce_uri_subtype_pb = require('../enumeration/joyce_uri_subtype_pb.js');

function serialize_OptionalSchema(arg) {
  if (!(arg instanceof model_schema_pb.OptionalSchema)) {
    throw new Error('Expected argument of type OptionalSchema');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_OptionalSchema(buffer_arg) {
  return model_schema_pb.OptionalSchema.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_RequestParams(arg) {
  if (!(arg instanceof api_schema_api_pb.RequestParams)) {
    throw new Error('Expected argument of type RequestParams');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_RequestParams(buffer_arg) {
  return api_schema_api_pb.RequestParams.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_Schema(arg) {
  if (!(arg instanceof model_schema_pb.Schema)) {
    throw new Error('Expected argument of type Schema');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_Schema(buffer_arg) {
  return model_schema_pb.Schema.deserializeBinary(new Uint8Array(buffer_arg));
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

function serialize_google_protobuf_StringValue(arg) {
  if (!(arg instanceof google_protobuf_wrappers_pb.StringValue)) {
    throw new Error('Expected argument of type google.protobuf.StringValue');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_google_protobuf_StringValue(buffer_arg) {
  return google_protobuf_wrappers_pb.StringValue.deserializeBinary(new Uint8Array(buffer_arg));
}


var SchemaApiService = exports.SchemaApiService = {
  getSchema: {
    path: '/SchemaApi/GetSchema',
    requestStream: false,
    responseStream: false,
    requestType: google_protobuf_wrappers_pb.StringValue,
    responseType: model_schema_pb.OptionalSchema,
    requestSerialize: serialize_google_protobuf_StringValue,
    requestDeserialize: deserialize_google_protobuf_StringValue,
    responseSerialize: serialize_OptionalSchema,
    responseDeserialize: deserialize_OptionalSchema,
  },
  getAllSchemas: {
    path: '/SchemaApi/GetAllSchemas',
    requestStream: false,
    responseStream: true,
    requestType: google_protobuf_wrappers_pb.StringValue,
    responseType: model_schema_pb.Schema,
    requestSerialize: serialize_google_protobuf_StringValue,
    requestDeserialize: deserialize_google_protobuf_StringValue,
    responseSerialize: serialize_Schema,
    responseDeserialize: deserialize_Schema,
  },
  getAllSchemasBySubtypeAndNamespace: {
    path: '/SchemaApi/GetAllSchemasBySubtypeAndNamespace',
    requestStream: false,
    responseStream: true,
    requestType: api_schema_api_pb.RequestParams,
    responseType: model_schema_pb.Schema,
    requestSerialize: serialize_RequestParams,
    requestDeserialize: deserialize_RequestParams,
    responseSerialize: serialize_Schema,
    responseDeserialize: deserialize_Schema,
  },
  getAllSchemasByReportsIsNotEmpty: {
    path: '/SchemaApi/GetAllSchemasByReportsIsNotEmpty',
    requestStream: false,
    responseStream: true,
    requestType: google_protobuf_empty_pb.Empty,
    responseType: model_schema_pb.Schema,
    requestSerialize: serialize_google_protobuf_Empty,
    requestDeserialize: deserialize_google_protobuf_Empty,
    responseSerialize: serialize_Schema,
    responseDeserialize: deserialize_Schema,
  },
  getAllNamespaces: {
    path: '/SchemaApi/GetAllNamespaces',
    requestStream: false,
    responseStream: true,
    requestType: google_protobuf_empty_pb.Empty,
    responseType: google_protobuf_wrappers_pb.StringValue,
    requestSerialize: serialize_google_protobuf_Empty,
    requestDeserialize: deserialize_google_protobuf_Empty,
    responseSerialize: serialize_google_protobuf_StringValue,
    responseDeserialize: deserialize_google_protobuf_StringValue,
  },
};

exports.SchemaApiClient = grpc.makeGenericClientConstructor(SchemaApiService);
