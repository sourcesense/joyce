package com.sourcesense.joyce.schemaengine.model.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptHandlerData {

	private String language;
	private boolean oneLine = true;
	private String code;
}
