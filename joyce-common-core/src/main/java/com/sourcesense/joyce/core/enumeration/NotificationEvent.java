/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.core.enumeration;

public enum NotificationEvent {

	NONE,

	RAW_DATA_SEND_SUCCESS,
	RAW_DATA_SEND_FAILED,

	IMPORT_FAILED_INVALID_MESSAGE_KEY,
	IMPORT_FAILED_INVALID_SCHEMA,
	IMPORT_INSERT_FAILED,
	IMPORT_REMOVE_FAILED,
	IMPORT_SCHEMA_REVERSE_CREATION_SUCCESS,


	MODEL_CONTENT_PROCESSING_FAILED,
	MODEL_CONTENT_INSERT_FAILED,
	MODEL_CONTENT_DELETE_FAILED,
	MODEL_CONTENT_DELETE_SUCCEDED,
	MODEL_CONTENT_INSERT_SUCCEDED,

	CONTENT_PUBLISH_SUCCESS,
	CONTENT_PUBLISH_FAILED,

	SCHEMA_PROCESSING_SUCCESS,
	SCHEMA_PROCESSING_FAILED,

	EXTRACT_CSV_ROW_FAILED,
	READ_CSV_HEADER_FAILED,
	READ_CSV_ROW_FAILED,

	// Sink Events
	SINK_MONGODB_STORE_SUCCESS,
	SINK_MONGODB_STORE_FAILED,
	SINK_MONGODB_DELETE_SUCCESS,

	//Schema Sink Events
	SINK_MONGODB_CREATE_COLLECTION_SUCCESS,
	SINK_MONGODB_CREATE_COLLECTION_FAILED,
	SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_SUCCESS,
	SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_FAILED,
	SINK_MONGODB_CREATE_INDEXES_SUCCESS,
	SINK_MONGODB_CREATE_INDEXES_FAILED, MODEL_CONTENT_PROCESS_ALL_FAILED,
}
