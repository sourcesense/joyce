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

package com.sourcesense.nile.core.enumeration;

public enum NotificationEvent {

    NONE,

    RAW_URI_GENERATION_FAILED,

    RAW_DATA_SEND_SUCCESS,
    RAW_DATA_SEND_FAILED,

    RAW_DATA_INSERT_FAILED,
    RAW_DATA_REMOVAL_FAILED,

    IMPORT_SCHEMA_CREATION_FAILED,
    IMPORT_SCHEMA_REVERSE_CREATION_SUCCESS,

    MAINLOG_PUBLISH_SUCCESS,
    MAINLOG_PUBLISH_FAILED,

    SCHEMA_PUBLISH_SUCCESS,
    SCHEMA_PUBLISH_FAILED,

    EXTRACT_CSV_ROW_FAILED,
    READ_CSV_HEADER_FAILED,
    READ_CSV_ROW_FAILED,

    // Sink Events
    SINK_MONGODB_STORED,
    SINK_MONGODB_FAILED,
    SINK_MONGODB_DELETED;
}
