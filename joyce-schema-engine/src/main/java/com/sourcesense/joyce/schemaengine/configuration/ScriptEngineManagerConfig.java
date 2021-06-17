package com.sourcesense.joyce.schemaengine.configuration;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.sourcesense.joyce.schemaengine.ScriptingEngine;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.python.jsr223.PyScriptEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.script.CompiledScript;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Configuration
public class ScriptEngineManagerConfig {

	@Bean
	ScriptEngineManager scriptEngineManager() {
		return new ScriptEngineManager();
	}

	@Bean
	@Primary
	GraalJSScriptEngine jsScriptEngine(ScriptEngineManager scriptEngineManager) {
		return (GraalJSScriptEngine) scriptEngineManager.getEngineByName(ScriptingEngine.JAVASCRIPT.getName());
	}

	@Bean
	PyScriptEngine pythonScriptEngine(ScriptEngineManager scriptEngineManager) {
		return (PyScriptEngine) scriptEngineManager.getEngineByName(ScriptingEngine.PYTHON.getName());
	}

	@Bean
	GroovyScriptEngineImpl groovyScriptEngine(ScriptEngineManager scriptEngineManager) {
		return (GroovyScriptEngineImpl) scriptEngineManager.getEngineByName(ScriptingEngine.GROOVY.getName());
	}

	public static void main(String[] args) throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		GraalJSScriptEngine engine = (GraalJSScriptEngine) manager.getEngineByName(ScriptingEngine.JAVASCRIPT.getName());
		engine.put("ciao", "ciao");
		CompiledScript script = engine.compile("print(ciao)");
		script.eval();

		manager.getEngineFactories().stream()
				.map(ScriptEngineFactory::getEngineName)
				.forEach(System.out::println);
	}
}
