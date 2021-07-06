package com.sourcesense.joyce.schemaengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptData {

	private String language;
	private boolean oneLine = true;
	private String code;
}
