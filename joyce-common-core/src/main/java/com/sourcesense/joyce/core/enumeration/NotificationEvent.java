package com.sourcesense.joyce.core.enumeration;

public class NotificationEvent {

	public static final String NONE = "NONE";

	public static final String IMPORT_ERROR_INVALID_SCHEMA = "IMPORT_ERROR_INVALID_SCHEMA";
	public static final String IMPORT_ERROR_INVALID_MESSAGE_KEY = "IMPORT_ERROR_INVALID_MESSAGE_KEY";
	public static final String IMPORT_INSERT_FAILED = "IMPORT_INSERT_FAILED";
	public static final String IMPORT_REMOVE_FAILED = "IMPORT_REMOVE_FAILED";
	public static final String IMPORT_BULK_INSERT_FAILED_INVALID_FILE = "IMPORT_BULK_INSERT_FAILED_INVALID_FILE";
	public static final String IMPORT_BULK_INSERT_SUCCESS = "IMPORT_BULK_INSERT_SUCCESS";

	public static final String CONTENT_PUBLISH_SUCCESS = "CONTENT_PUBLISH_SUCCESS";
	public static final String CONTENT_PUBLISH_FAILED = "CONTENT_PUBLISH_FAILED";

	public static final String COMMAND_PUBLISH_SUCCESS = "COMMAND_PUBLISH_SUCCESS";
	public static final String COMMAND_PUBLISH_FAILED = "COMMAND_PUBLISH_FAILED";

	public static final String SCHEMA_PUBLISH_SUCCESS = "SCHEMA_PUBLISH_SUCCESS";
	public static final String SCHEMA_PUBLISH_FAILED = "SCHEMA_PUBLISH_FAILED";

	// Sink Events
	public static final String SINK_MONGODB_ERROR_INVALID_MESSAGE_KEY = "SINK_MONGODB_ERROR_INVALID_MESSAGE_KEY";
	public static final String SINK_MONGODB_STORE_SUCCESS = "SINK_MONGODB_STORE_SUCCESS";
	public static final String SINK_MONGODB_STORE_FAILED = "SINK_MONGODB_STORE_FAILED";
	public static final String SINK_MONGODB_DELETE_SUCCESS = "SINK_MONGODB_DELETE_SUCCESS";
	public static final String SINK_MONGODB_DELETE_FAILED = "SINK_MONGODB_DELETE_FAILED";

	//Schema Sink Events
	public static final String SINK_MONGODB_SCHEMA_PARSING_FAILED = "SINK_MONGODB_SCHEMA_PARSING_FAILED";
	public static final String SINK_MONGODB_CREATE_COLLECTION_SUCCESS = "SINK_MONGODB_CREATE_COLLECTION_SUCCESS";
	public static final String SINK_MONGODB_CREATE_COLLECTION_FAILED = "SINK_MONGODB_CREATE_COLLECTION_FAILED";
	public static final String SINK_MONGODB_DELETE_COLLECTION_SUCCESS = "SINK_MONGODB_DELETE_COLLECTION_SUCCESS";
	public static final String SINK_MONGODB_DELETE_COLLECTION_FAILED = "SINK_MONGODB_DELETE_COLLECTION_FAILED";
	public static final String SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_SUCCESS = "SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_SUCCESS";
	public static final String SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_FAILED = "SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_FAILED";
	public static final String SINK_MONGODB_CREATE_INDEXES_SUCCESS = "SINK_MONGODB_CREATE_INDEXES_SUCCESS";
	public static final String SINK_MONGODB_CREATE_INDEXES_FAILED = "SINK_MONGODB_CREATE_INDEXES_FAILED";
}
