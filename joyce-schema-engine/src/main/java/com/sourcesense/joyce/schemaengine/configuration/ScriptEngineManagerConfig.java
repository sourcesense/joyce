package com.sourcesense.joyce.schemaengine.configuration;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.sourcesense.joyce.schemaengine.enumeration.ScriptingEngine;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.graalvm.polyglot.Context;
import org.python.jsr223.PyScriptEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.script.ScriptEngineManager;

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
		return (PyScriptEngine) scriptEngineManager.getEngineByName(ScriptingEngine.PYTHON.getEngineName());
	}

	@Bean
	GroovyScriptEngineImpl groovyScriptEngine() {
		ImportCustomizer imports = new ImportCustomizer();
		imports.addStarImports("groovy.json");

		CompilerConfiguration config = new CompilerConfiguration();
		config.addCompilationCustomizers(imports);

		return new GroovyScriptEngineImpl(new GroovyClassLoader(
				Thread.currentThread().getContextClassLoader(),
				config
		));
	}
}
