package com.sourcesense.joyce.schemaengine.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.sourcesense.joyce.schemaengine.annotation.HandlebarsHelper;
import com.sourcesense.joyce.schemaengine.exception.HandlebarsHelperTagNotFoundException;
import com.sourcesense.joyce.schemaengine.templating.handlebars.resolver.HandlebarsTemplateResolver;
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
public class HandlebarsConfig {

	private final ObjectMapper jsonMapper;
	private final ApplicationContext context;


	@Bean
	public HandlebarsTemplateResolver handlebarTemplateResolver(Handlebars handlebars) {
		return new HandlebarsTemplateResolver(jsonMapper, handlebars);
	}

	@Bean
	public Handlebars handlebars(@Qualifier("handlebarsHelpers") Map<String, Helper<?>> handlebarHelpers) {
		Handlebars handlebars = new Handlebars();
		handlebarHelpers.forEach(handlebars::registerHelper);
		return handlebars;
	}

	@Bean
	public Map<String, Helper<?>> handlebarsHelpers() {
		return context.getBeansWithAnnotation(HandlebarsHelper.class)
				.values().stream()
				.filter(helper -> helper instanceof Helper<?>)
				.map(helper -> (Helper<?>) helper)
				.collect(Collectors.toMap(
						this::computeTag,
						Function.identity()
				));
	}

	private String computeTag(Helper<?> helper) {
		return Optional.of(helper)
				.map(Helper::getClass)
				.map(clazz -> clazz.getAnnotation(HandlebarsHelper.class))
				.map(HandlebarsHelper::tag)
				.filter(Predicate.not(String::isEmpty))
				.orElseThrow(() -> new HandlebarsHelperTagNotFoundException(
						String.format("An handlebars helper annotated with @%s must have a tag.", HandlebarsHelper.class.getSimpleName())
				));
	}
}
