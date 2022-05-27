package com.sourcesense.joyce.schemaengine.model.dto.handler;

import com.fasterxml.jackson.annotation.JsonAlias;
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
