package com.sourcesense.joyce.schemaengine.configuration;

import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.exception.InvalidHandlerKeywordException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
@DependsOn("pluginManager")
@ConditionalOnProperty(value = "joyce.data.mongodb.enabled", havingValue = "true")
public class SchemaTransformerConfig {

    private final ApplicationContext applicationContext;

	/**
	 *
	 * This method builds a map that contains all the transformer handler used in the import process.
	 * Handlers retrieved from plugins will be included in the map.
	 * All the handlers must be annotated with {@link SchemaTransformationHandler} annotation and must
	 * implement {@link SchemaTransformerHandler} interface.
	 * 	The handlers are retrieved from spring context.
	 *
	 *
	 * @return Map with handler keyword as key and handler as value as a Spring Bean
	 */
	@Bean
    public Map<String, SchemaTransformerHandler> transformerHandlers() {
        Map<String, SchemaTransformerHandler> transformerHandlers = applicationContext
                .getBeansWithAnnotation(SchemaTransformationHandler.class).values().stream()
                .map(this::castToSchemaTransformerHandler)
                .collect(Collectors.toMap(
                        this::computeKeyword,
                        Function.identity()
                        )
                );

        log.info(
                "The following {} schema transformer handlers have been loaded: {}",
                transformerHandlers.size(), transformerHandlers.keySet()
        );

        return transformerHandlers;
    }

	/**
	 * This method retrieves keywords from transformation handlers and normalizes them adding
	 * $ symbol if it's not present.
	 *
	 * @param handler Schema Transformer Handler
	 * @return Handler keyword
	 */
	private String computeKeyword(SchemaTransformerHandler handler) {
        return Optional.ofNullable(handler)
                .map(SchemaTransformerHandler::getClass)
                .map(handlerClass -> handlerClass.getAnnotation(SchemaTransformationHandler.class))
                .map(SchemaTransformationHandler::keyword)
                .filter(StringUtils::isNotEmpty)
                .map(this::computeNormalizedKeyword)
                .orElseThrow(() -> new InvalidHandlerKeywordException(
                        String.format("Impossible to retrieve keyword for transformer handler '%s'", handler.getClass().getSimpleName())
                ));
    }

    private SchemaTransformerHandler castToSchemaTransformerHandler(Object bean) {
        try {
            return (SchemaTransformerHandler) bean;

        } catch (Exception exception) {
            throw new JoyceSchemaEngineException(String.format(
                    "Error happened while casting a bean of '%s' class." +
                    "Only a class that implements '%s' interface can be annotated with '%s' annotation.",
                    bean.getClass().getName(),
                    SchemaTransformerHandler.class.getName(),
                    SchemaTransformationHandler.class.getName()
            ));
        }
    }

    private String computeNormalizedKeyword(String keyword) {
        return !keyword.startsWith("$") ? "$".concat(keyword) : keyword;
    }
}
