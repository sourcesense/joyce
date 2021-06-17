package com.sourcesense.joyce.schemaengine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScriptingEngine {

	JAVASCRIPT("Graal.js"),
	PYTHON("jython"),
	GROOVY("Groovy Scripting Engine");

	private final String name;

}
