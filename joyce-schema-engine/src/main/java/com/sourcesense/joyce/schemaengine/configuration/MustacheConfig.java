package com.sourcesense.joyce.schemaengine.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samskivert.mustache.Mustache;
import com.sourcesense.joyce.schemaengine.annotation.MustacheLambda;
import com.sourcesense.joyce.schemaengine.exception.MustacheLambdaTagNotFoundException;
import com.sourcesense.joyce.schemaengine.templating.mustache.resolver.MustacheTemplateResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class MustacheConfig {

	private final ObjectMapper jsonMapper;
	private final ApplicationContext context;

	@Bean
	public MustacheTemplateResolver mustacheTemplate(@Qualifier("mustacheLambdas") Map<String, Mustache.Lambda> mustacheLambdas) {
		return new MustacheTemplateResolver(jsonMapper, Mustache.compiler(), mustacheLambdas);
	}

	@Bean
	public Map<String, Mustache.Lambda> mustacheLambdas() {
		return context.getBeansWithAnnotation(MustacheLambda.class)
				.values().stream()
				.filter(Mustache.Lambda.class::isInstance)
				.map(Mustache.Lambda.class::cast)
				.collect(Collectors.toMap(
						this::computeTag,
						Function.identity()
				));
	}

	private String computeTag(Mustache.Lambda lambda) {
		return Optional.of(lambda)
				.map(Mustache.Lambda::getClass)
				.map(clazz -> clazz.getAnnotation(MustacheLambda.class))
				.map(MustacheLambda::tag)
				.filter(Predicate.not(String::isEmpty))
				.orElseThrow(() -> new MustacheLambdaTagNotFoundException(
						String.format("A mustache lambda annotated with @%s must have a tag.", MustacheLambda.class.getSimpleName())
				));
	}
}