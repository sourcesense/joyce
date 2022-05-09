/*
 * Copyright 2021 Sourcesense Spa
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

package com.sourcesense.joyce.importcore.dto;

import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import lombok.Data;

/**
 *  This class maps the structure of the messageKey coming
 *  from a kafka connect
 */
@Data
public class ConnectKeyPayload {

	private JoyceSchemaURI schema;
	private String origin;
	private String uid;
}
