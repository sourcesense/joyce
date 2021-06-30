package com.sourcesense.joyce.schemaengine.configuration;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.sourcesense.joyce.schemaengine.enumeration.ScriptingEngine;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.python.jsr223.PyScriptEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.script.CompiledScript;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Configuration
public class ScriptEngineManagerConfig {

	@Bean
	ScriptEngineManager scriptEngineManager() {
		return new ScriptEngineManager();
	}

	@Bean
	@Primary
	GraalJSScriptEngine jsScriptEngine() {
		return GraalJSScriptEngine.create(
				null,
				Context.newBuilder("js").option("js.ecmascript-version", "2021")
		);
	}

	@Bean
	PyScriptEngine pythonScriptEngine(ScriptEngineManager scriptEngineManager) {
		return (PyScriptEngine) scriptEngineManager.getEngineByName(ScriptingEngine.PYTHON.getName());
	}

	@Bean
	GroovyScriptEngineImpl groovyScriptEngine(ScriptEngineManager scriptEngineManager) {
		return (GroovyScriptEngineImpl) scriptEngineManager.getEngineByName(ScriptingEngine.GROOVY.getName());
	}
}
