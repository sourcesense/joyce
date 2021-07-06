package com.sourcesense.joyce.schemaengine.enumeration;

import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.scripting.GroovyScriptingService;
import com.sourcesense.joyce.schemaengine.service.scripting.JavaScriptScriptingService;
import com.sourcesense.joyce.schemaengine.service.scripting.PythonScriptingService;
import com.sourcesense.joyce.schemaengine.service.scripting.ScriptingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ScriptingEngine {

	JAVASCRIPT("javascript", "Graal.js", JavaScriptScriptingService.class),
	PYTHON("python", "jython", PythonScriptingService.class),
	GROOVY("groovy", "Groovy Scripting Engine", GroovyScriptingService.class);

	private final String language;
	private final String engineName;
	private final Class<? extends ScriptingService> serviceClass;

	private final static Map<String, Class<? extends ScriptingService>> engineClassSelector;

	static {
		engineClassSelector = Arrays.stream(values())
				.collect(Collectors.toMap(
						ScriptingEngine::getLanguage,
						ScriptingEngine::getServiceClass
				));
	}

	public static ScriptingService getScriptingService(
			String language,
			ApplicationContext applicationContext) {

			return Optional.of(engineClassSelector)
					.map(selector -> selector.get(language))
					.map(applicationContext::getBean)
					.orElseThrow(
							() -> new JoyceSchemaEngineException("Impossible to retrieve scripting service from application context")
					);
	}
}
