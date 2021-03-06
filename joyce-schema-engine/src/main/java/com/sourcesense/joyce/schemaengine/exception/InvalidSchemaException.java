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

package com.sourcesense.joyce.schemaengine.exception;

import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidationResult;

import java.util.stream.Collectors;

public class InvalidSchemaException extends JoyceSchemaEngineException {
	public ValidationResult getValidationResult() {
		return validationResult;
	}

	private ValidationResult validationResult;

	public InvalidSchemaException(ValidationResult validation) {
		super(validation.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.joining()));
		this.validationResult = validation;
	}
}
