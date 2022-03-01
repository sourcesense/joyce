// source: api/schema_api.proto
/**
 * @fileoverview
 * @enhanceable
 * @suppress {missingRequire} reports error on implicit type usages.
 * @suppress {messageConventions} JS Compiler reports an error if a variable or
 *     field starts with 'MSG_' and isn't a translatable message.
 * @public
 */
// GENERATED CODE -- DO NOT EDIT!
/* eslint-disable */
// @ts-nocheck

var jspb = require('google-protobuf');
var goog = jspb;
var global = Function('return this')();

var google_protobuf_empty_pb = require('google-protobuf/google/protobuf/empty_pb.js');
goog.object.extend(proto, google_protobuf_empty_pb);
var model_schema_pb = require('../model/schema_pb.js');
goog.object.extend(proto, model_schema_pb);
var enumeration_joyce_uri_subtype_pb = require('../enumeration/joyce_uri_subtype_pb.js');
goog.object.extend(proto, enumeration_joyce_uri_subtype_pb);
goog.exportSymbol('proto.GetAllSchemasBySubtypeAndNamespaceRequest', null, global);
goog.exportSymbol('proto.GetAllSchemasRequest', null, global);
goog.exportSymbol('proto.GetNamespacesResponse', null, global);
goog.exportSymbol('proto.GetSchemaRequest', null, global);
goog.exportSymbol('proto.GetSchemaResponse', null, global);
goog.exportSymbol('proto.GetSchemasResponse', null, global);
/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.GetSchemaRequest = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.GetSchemaRequest, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  /**
   * @public
   * @override
   */
  proto.GetSchemaRequest.displayName = 'proto.GetSchemaRequest';
}
/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.GetAllSchemasRequest = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.GetAllSchemasRequest, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  /**
   * @public
   * @override
   */
  proto.GetAllSchemasRequest.displayName = 'proto.GetAllSchemasRequest';
}
/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.GetAllSchemasBySubtypeAndNamespaceRequest, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  /**
   * @public
   * @override
   */
  proto.GetAllSchemasBySubtypeAndNamespaceRequest.displayName = 'proto.GetAllSchemasBySubtypeAndNamespaceRequest';
}
/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.GetSchemaResponse = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.GetSchemaResponse, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  /**
   * @public
   * @override
   */
  proto.GetSchemaResponse.displayName = 'proto.GetSchemaResponse';
}
/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.GetSchemasResponse = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, proto.GetSchemasResponse.repeatedFields_, null);
};
goog.inherits(proto.GetSchemasResponse, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  /**
   * @public
   * @override
   */
  proto.GetSchemasResponse.displayName = 'proto.GetSchemasResponse';
}
/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.GetNamespacesResponse = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, proto.GetNamespacesResponse.repeatedFields_, null);
};
goog.inherits(proto.GetNamespacesResponse, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  /**
   * @public
   * @override
   */
  proto.GetNamespacesResponse.displayName = 'proto.GetNamespacesResponse';
}



if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * Optional fields that are not set will be set to undefined.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     net/proto2/compiler/js/internal/generator.cc#kKeyword.
 * @param {boolean=} opt_includeInstance Deprecated. whether to include the
 *     JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @return {!Object}
 */
proto.GetSchemaRequest.prototype.toObject = function(opt_includeInstance) {
  return proto.GetSchemaRequest.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Deprecated. Whether to include
 *     the JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.GetSchemaRequest} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetSchemaRequest.toObject = function(includeInstance, msg) {
  var f, obj = {
    id: jspb.Message.getFieldWithDefault(msg, 1, "")
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.GetSchemaRequest}
 */
proto.GetSchemaRequest.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.GetSchemaRequest;
  return proto.GetSchemaRequest.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.GetSchemaRequest} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.GetSchemaRequest}
 */
proto.GetSchemaRequest.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setId(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.GetSchemaRequest.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.GetSchemaRequest.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.GetSchemaRequest} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetSchemaRequest.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getId();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
};


/**
 * optional string id = 1;
 * @return {string}
 */
proto.GetSchemaRequest.prototype.getId = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 1, ""));
};


/**
 * @param {string} value
 * @return {!proto.GetSchemaRequest} returns this
 */
proto.GetSchemaRequest.prototype.setId = function(value) {
  return jspb.Message.setProto3StringField(this, 1, value);
};





if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * Optional fields that are not set will be set to undefined.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     net/proto2/compiler/js/internal/generator.cc#kKeyword.
 * @param {boolean=} opt_includeInstance Deprecated. whether to include the
 *     JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @return {!Object}
 */
proto.GetAllSchemasRequest.prototype.toObject = function(opt_includeInstance) {
  return proto.GetAllSchemasRequest.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Deprecated. Whether to include
 *     the JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.GetAllSchemasRequest} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetAllSchemasRequest.toObject = function(includeInstance, msg) {
  var f, obj = {
    rootonly: jspb.Message.getFieldWithDefault(msg, 1, "")
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.GetAllSchemasRequest}
 */
proto.GetAllSchemasRequest.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.GetAllSchemasRequest;
  return proto.GetAllSchemasRequest.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.GetAllSchemasRequest} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.GetAllSchemasRequest}
 */
proto.GetAllSchemasRequest.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setRootonly(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.GetAllSchemasRequest.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.GetAllSchemasRequest.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.GetAllSchemasRequest} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetAllSchemasRequest.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getRootonly();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
};


/**
 * optional string rootOnly = 1;
 * @return {string}
 */
proto.GetAllSchemasRequest.prototype.getRootonly = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 1, ""));
};


/**
 * @param {string} value
 * @return {!proto.GetAllSchemasRequest} returns this
 */
proto.GetAllSchemasRequest.prototype.setRootonly = function(value) {
  return jspb.Message.setProto3StringField(this, 1, value);
};





if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * Optional fields that are not set will be set to undefined.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     net/proto2/compiler/js/internal/generator.cc#kKeyword.
 * @param {boolean=} opt_includeInstance Deprecated. whether to include the
 *     JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @return {!Object}
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.toObject = function(opt_includeInstance) {
  return proto.GetAllSchemasBySubtypeAndNamespaceRequest.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Deprecated. Whether to include
 *     the JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.GetAllSchemasBySubtypeAndNamespaceRequest} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.toObject = function(includeInstance, msg) {
  var f, obj = {
    subtype: jspb.Message.getFieldWithDefault(msg, 1, 0),
    namespace: jspb.Message.getFieldWithDefault(msg, 2, ""),
    rootonly: jspb.Message.getFieldWithDefault(msg, 3, "")
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.GetAllSchemasBySubtypeAndNamespaceRequest}
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.GetAllSchemasBySubtypeAndNamespaceRequest;
  return proto.GetAllSchemasBySubtypeAndNamespaceRequest.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.GetAllSchemasBySubtypeAndNamespaceRequest} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.GetAllSchemasBySubtypeAndNamespaceRequest}
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {!proto.JoyceUriSubtype} */ (reader.readEnum());
      msg.setSubtype(value);
      break;
    case 2:
      var value = /** @type {string} */ (reader.readString());
      msg.setNamespace(value);
      break;
    case 3:
      var value = /** @type {string} */ (reader.readString());
      msg.setRootonly(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.GetAllSchemasBySubtypeAndNamespaceRequest.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.GetAllSchemasBySubtypeAndNamespaceRequest} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getSubtype();
  if (f !== 0.0) {
    writer.writeEnum(
      1,
      f
    );
  }
  f = message.getNamespace();
  if (f.length > 0) {
    writer.writeString(
      2,
      f
    );
  }
  f = message.getRootonly();
  if (f.length > 0) {
    writer.writeString(
      3,
      f
    );
  }
};


/**
 * optional JoyceUriSubtype subtype = 1;
 * @return {!proto.JoyceUriSubtype}
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.getSubtype = function() {
  return /** @type {!proto.JoyceUriSubtype} */ (jspb.Message.getFieldWithDefault(this, 1, 0));
};


/**
 * @param {!proto.JoyceUriSubtype} value
 * @return {!proto.GetAllSchemasBySubtypeAndNamespaceRequest} returns this
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.setSubtype = function(value) {
  return jspb.Message.setProto3EnumField(this, 1, value);
};


/**
 * optional string namespace = 2;
 * @return {string}
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.getNamespace = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 2, ""));
};


/**
 * @param {string} value
 * @return {!proto.GetAllSchemasBySubtypeAndNamespaceRequest} returns this
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.setNamespace = function(value) {
  return jspb.Message.setProto3StringField(this, 2, value);
};


/**
 * optional string rootOnly = 3;
 * @return {string}
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.getRootonly = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 3, ""));
};


/**
 * @param {string} value
 * @return {!proto.GetAllSchemasBySubtypeAndNamespaceRequest} returns this
 */
proto.GetAllSchemasBySubtypeAndNamespaceRequest.prototype.setRootonly = function(value) {
  return jspb.Message.setProto3StringField(this, 3, value);
};





if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * Optional fields that are not set will be set to undefined.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     net/proto2/compiler/js/internal/generator.cc#kKeyword.
 * @param {boolean=} opt_includeInstance Deprecated. whether to include the
 *     JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @return {!Object}
 */
proto.GetSchemaResponse.prototype.toObject = function(opt_includeInstance) {
  return proto.GetSchemaResponse.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Deprecated. Whether to include
 *     the JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.GetSchemaResponse} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetSchemaResponse.toObject = function(includeInstance, msg) {
  var f, obj = {
    schema: (f = msg.getSchema()) && model_schema_pb.Schema.toObject(includeInstance, f)
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.GetSchemaResponse}
 */
proto.GetSchemaResponse.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.GetSchemaResponse;
  return proto.GetSchemaResponse.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.GetSchemaResponse} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.GetSchemaResponse}
 */
proto.GetSchemaResponse.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = new model_schema_pb.Schema;
      reader.readMessage(value,model_schema_pb.Schema.deserializeBinaryFromReader);
      msg.setSchema(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.GetSchemaResponse.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.GetSchemaResponse.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.GetSchemaResponse} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetSchemaResponse.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getSchema();
  if (f != null) {
    writer.writeMessage(
      1,
      f,
      model_schema_pb.Schema.serializeBinaryToWriter
    );
  }
};


/**
 * optional Schema schema = 1;
 * @return {?proto.Schema}
 */
proto.GetSchemaResponse.prototype.getSchema = function() {
  return /** @type{?proto.Schema} */ (
    jspb.Message.getWrapperField(this, model_schema_pb.Schema, 1));
};


/**
 * @param {?proto.Schema|undefined} value
 * @return {!proto.GetSchemaResponse} returns this
*/
proto.GetSchemaResponse.prototype.setSchema = function(value) {
  return jspb.Message.setWrapperField(this, 1, value);
};


/**
 * Clears the message field making it undefined.
 * @return {!proto.GetSchemaResponse} returns this
 */
proto.GetSchemaResponse.prototype.clearSchema = function() {
  return this.setSchema(undefined);
};


/**
 * Returns whether this field is set.
 * @return {boolean}
 */
proto.GetSchemaResponse.prototype.hasSchema = function() {
  return jspb.Message.getField(this, 1) != null;
};



/**
 * List of repeated fields within this message type.
 * @private {!Array<number>}
 * @const
 */
proto.GetSchemasResponse.repeatedFields_ = [1];



if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * Optional fields that are not set will be set to undefined.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     net/proto2/compiler/js/internal/generator.cc#kKeyword.
 * @param {boolean=} opt_includeInstance Deprecated. whether to include the
 *     JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @return {!Object}
 */
proto.GetSchemasResponse.prototype.toObject = function(opt_includeInstance) {
  return proto.GetSchemasResponse.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Deprecated. Whether to include
 *     the JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.GetSchemasResponse} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetSchemasResponse.toObject = function(includeInstance, msg) {
  var f, obj = {
    schemasList: jspb.Message.toObjectList(msg.getSchemasList(),
    model_schema_pb.Schema.toObject, includeInstance)
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.GetSchemasResponse}
 */
proto.GetSchemasResponse.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.GetSchemasResponse;
  return proto.GetSchemasResponse.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.GetSchemasResponse} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.GetSchemasResponse}
 */
proto.GetSchemasResponse.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = new model_schema_pb.Schema;
      reader.readMessage(value,model_schema_pb.Schema.deserializeBinaryFromReader);
      msg.addSchemas(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.GetSchemasResponse.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.GetSchemasResponse.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.GetSchemasResponse} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetSchemasResponse.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getSchemasList();
  if (f.length > 0) {
    writer.writeRepeatedMessage(
      1,
      f,
      model_schema_pb.Schema.serializeBinaryToWriter
    );
  }
};


/**
 * repeated Schema schemas = 1;
 * @return {!Array<!proto.Schema>}
 */
proto.GetSchemasResponse.prototype.getSchemasList = function() {
  return /** @type{!Array<!proto.Schema>} */ (
    jspb.Message.getRepeatedWrapperField(this, model_schema_pb.Schema, 1));
};


/**
 * @param {!Array<!proto.Schema>} value
 * @return {!proto.GetSchemasResponse} returns this
*/
proto.GetSchemasResponse.prototype.setSchemasList = function(value) {
  return jspb.Message.setRepeatedWrapperField(this, 1, value);
};


/**
 * @param {!proto.Schema=} opt_value
 * @param {number=} opt_index
 * @return {!proto.Schema}
 */
proto.GetSchemasResponse.prototype.addSchemas = function(opt_value, opt_index) {
  return jspb.Message.addToRepeatedWrapperField(this, 1, opt_value, proto.Schema, opt_index);
};


/**
 * Clears the list making it empty but non-null.
 * @return {!proto.GetSchemasResponse} returns this
 */
proto.GetSchemasResponse.prototype.clearSchemasList = function() {
  return this.setSchemasList([]);
};



/**
 * List of repeated fields within this message type.
 * @private {!Array<number>}
 * @const
 */
proto.GetNamespacesResponse.repeatedFields_ = [1];



if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * Optional fields that are not set will be set to undefined.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     net/proto2/compiler/js/internal/generator.cc#kKeyword.
 * @param {boolean=} opt_includeInstance Deprecated. whether to include the
 *     JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @return {!Object}
 */
proto.GetNamespacesResponse.prototype.toObject = function(opt_includeInstance) {
  return proto.GetNamespacesResponse.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Deprecated. Whether to include
 *     the JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.GetNamespacesResponse} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetNamespacesResponse.toObject = function(includeInstance, msg) {
  var f, obj = {
    namespacesList: (f = jspb.Message.getRepeatedField(msg, 1)) == null ? undefined : f
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.GetNamespacesResponse}
 */
proto.GetNamespacesResponse.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.GetNamespacesResponse;
  return proto.GetNamespacesResponse.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.GetNamespacesResponse} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.GetNamespacesResponse}
 */
proto.GetNamespacesResponse.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.addNamespaces(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.GetNamespacesResponse.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.GetNamespacesResponse.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.GetNamespacesResponse} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.GetNamespacesResponse.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getNamespacesList();
  if (f.length > 0) {
    writer.writeRepeatedString(
      1,
      f
    );
  }
};


/**
 * repeated string namespaces = 1;
 * @return {!Array<string>}
 */
proto.GetNamespacesResponse.prototype.getNamespacesList = function() {
  return /** @type {!Array<string>} */ (jspb.Message.getRepeatedField(this, 1));
};


/**
 * @param {!Array<string>} value
 * @return {!proto.GetNamespacesResponse} returns this
 */
proto.GetNamespacesResponse.prototype.setNamespacesList = function(value) {
  return jspb.Message.setField(this, 1, value || []);
};


/**
 * @param {string} value
 * @param {number=} opt_index
 * @return {!proto.GetNamespacesResponse} returns this
 */
proto.GetNamespacesResponse.prototype.addNamespaces = function(value, opt_index) {
  return jspb.Message.addToRepeatedField(this, 1, value, opt_index);
};


/**
 * Clears the list making it empty but non-null.
 * @return {!proto.GetNamespacesResponse} returns this
 */
proto.GetNamespacesResponse.prototype.clearNamespacesList = function() {
  return this.setNamespacesList([]);
};


goog.object.extend(exports, proto);
