package com.sourcesense.nile.schemaengine.configuration;

import com.sourcesense.nile.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.nile.schemaengine.exception.InvalidHandlerKeywordException;
import com.sourcesense.nile.schemaengine.exception.NileSchemaEngineException;
import com.sourcesense.nile.schemaengine.handler.SchemaTransformerHandler;
import lombok.RequiredArgsConstructor;
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

@Configuration
@RequiredArgsConstructor
@DependsOn("pluginManager")
public class SchemaTransformerConfig {

    private final ApplicationContext applicationContext;

    @Bean
    public Map<String, SchemaTransformerHandler> transformerHandlers() {
        return applicationContext.getBeansWithAnnotation(SchemaTransformationHandler.class).values().stream()
                .map(this::castToSchemaTransformerHandler)
                .collect(Collectors.toMap(
                        this::computeKeyword,
                        Function.identity()
                        )
                );
    }

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
            throw new NileSchemaEngineException(String.format(
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
