package com.sourcesense.nile.core.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MappersConfig {

    @Bean
    @Primary
    ObjectMapper jsonMapper() {
        return new ObjectMapper();
    }

    @Bean
    ObjectMapper secondaryJsonMapper() {
        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Bean
    CsvMapper csvMapper() {
        return new CsvMapper()
                .enable(CsvParser.Feature.TRIM_SPACES)
                .enable(CsvParser.Feature.ALLOW_COMMENTS)
                .enable(CsvParser.Feature.ALLOW_TRAILING_COMMA)
                .enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
                .enable(CsvParser.Feature.SKIP_EMPTY_LINES);
    }

    @Bean
    CsvMapper arrayWrappingCsvMapper() {
        return new CsvMapper()
                .enable(CsvParser.Feature.TRIM_SPACES)
                .enable(CsvParser.Feature.ALLOW_COMMENTS)
                .enable(CsvParser.Feature.ALLOW_TRAILING_COMMA)
                .enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
                .enable(CsvParser.Feature.SKIP_EMPTY_LINES)
                .enable(CsvParser.Feature.WRAP_AS_ARRAY);
    }

    @Bean
    YAMLMapper yamlMapper() {
        YAMLMapper yamlMapper = new YAMLMapper();
        yamlMapper.disable(YAMLGenerator.Feature.SPLIT_LINES);
        yamlMapper.enable(YAMLGenerator.Feature.INDENT_ARRAYS);
        yamlMapper.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        yamlMapper.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
        yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        return yamlMapper;
    }
}
