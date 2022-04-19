package com.sourcesense.joyce.schemaengine.templating.mustache.template;

import com.samskivert.mustache.Mustache;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MustacheTemplate {

	private final Mustache.Compiler mustacheCompiler;
	private final Map<String, Object> mustacheContext;

	public String resolve(String template) {
		return mustacheCompiler
				.compile(template)
				.execute(mustacheContext);
	}
}
