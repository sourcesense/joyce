package com.sourcesense.joyce.schemaengine.utility;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.sourcesense.joyce.schemaengine.enumeration.ScriptingEngine;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.graalvm.polyglot.Context;
import org.python.jsr223.PyScriptEngine;

import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public interface UtilitySupplier {

	default ObjectMapper initMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	default JsonNode getResourceAsNode(String path) throws IOException, URISyntaxException {
		URL res = this.getClass().getClassLoader().getResource(path);

		return new ObjectMapper().readValue(
				Files.readAllBytes(Path.of(res.toURI())),
				JsonNode.class
		);
	}

	default GraalJSScriptEngine initGraalJSScriptEngine() {
		return GraalJSScriptEngine.create(
				null,
				Context.newBuilder("js")
						.option("js.ecmascript-version", "2021")
		);
	}

	default PyScriptEngine initPyScriptEngine() {
		return (PyScriptEngine) new ScriptEngineManager().getEngineByName(ScriptingEngine.PYTHON.getEngineName());
	}

	default GroovyScriptEngineImpl initGroovyScriptEngine() {
		ImportCustomizer imports = new ImportCustomizer();
		imports.addStarImports("groovy.json");

		CompilerConfiguration config = new CompilerConfiguration();
		config.addCompilationCustomizers(imports);

		GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
		return new GroovyScriptEngineImpl(classLoader);
	}
}
