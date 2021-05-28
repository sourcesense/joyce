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

public class KafkaCustomHeaders {

    public static final String RAW_URI          = "X-Joyce-Raw-Uri";
    public static final String MESSAGE_ACTION   = "X-Joyce-Action";
    public static final String IMPORT_SCHEMA    = "X-Joyce-Schema";
    public static final String COLLECTION       = "X-Joyce-collection";
    public static final String SCHEMA           = "X-Joyce-Schema-Name";
    public static final String PARENT           = "X-Joyce-Schema-Parent";
    public static final String SUBTYPE          = "X-Joyce-Schema-Subtype";
}
