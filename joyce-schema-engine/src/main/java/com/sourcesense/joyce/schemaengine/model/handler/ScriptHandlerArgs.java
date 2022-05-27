package com.sourcesense.joyce.schemaengine.model.handler;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptHandlerArgs {

	@JsonAlias(value = {"lang", "language"})
	private String language;
	private boolean oneLine = true;
	private String code;
}
