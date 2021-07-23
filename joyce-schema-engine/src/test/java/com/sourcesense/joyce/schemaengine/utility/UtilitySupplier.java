package com.sourcesense.joyce.schemaengine.utility;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.sourcesense.joyce.schemaengine.enumeration.ScriptingEngine;
import com.sourcesense.joyce.schemaengine.service.scripting.GroovyScriptingService;
import com.sourcesense.joyce.schemaengine.service.scripting.JavaScriptScriptingService;
import com.sourcesense.joyce.schemaengine.service.scripting.PythonScriptingService;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.graalvm.polyglot.Context;
import org.python.jsr223.PyScriptEngine;
import org.springframework.context.ApplicationContext;

import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public interface UtilitySupplier {

	default ObjectMapper initJsonMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	default YAMLMapper initYamlMapper() {
		YAMLMapper yamlMapper = new YAMLMapper();
		yamlMapper.disable(YAMLGenerator.Feature.SPLIT_LINES);
		yamlMapper.enable(YAMLGenerator.Feature.INDENT_ARRAYS);
		yamlMapper.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
		yamlMapper.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
		yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
		return yamlMapper;
	}

	default JsonNode getResourceAsNode(String path) throws IOException, URISyntaxException {
		URL res = this.getClass().getClassLoader().getResource(path);
		return new ObjectMapper().readValue(
				Files.readAllBytes(Path.of(res.toURI())),
				JsonNode.class
		);
	}

	default String getResourceAsString(String path) throws IOException, URISyntaxException {
		URL res = this.getClass().getClassLoader().getResource(path);
		return Files.readString(Path.of(res.toURI()));
	}

	default Path loadResource(String name) throws URISyntaxException {
		URL res = this.getClass().getClassLoader().getResource(name);
		return Paths.get(res.toURI());
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

	default ApplicationContext initApplicationContext(ObjectMapper mapper) {
		ApplicationContext context = mock(ApplicationContext.class);
		JavaScriptScriptingService jsService = new JavaScriptScriptingService(mapper, this.initGraalJSScriptEngine());
		PythonScriptingService pyService = new PythonScriptingService(mapper, this.initPyScriptEngine());
		GroovyScriptingService groovyService = new GroovyScriptingService(mapper, this.initGroovyScriptEngine());
		when(context.getBean(JavaScriptScriptingService.class)).thenReturn(jsService);
		when(context.getBean(PythonScriptingService.class)).thenReturn(pyService);
		when(context.getBean(GroovyScriptingService.class)).thenReturn(groovyService);
		return context;
	}
}
