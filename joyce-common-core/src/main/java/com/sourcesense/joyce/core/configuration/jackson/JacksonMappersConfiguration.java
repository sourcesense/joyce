/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.core.configuration.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

@Configuration
public class JacksonMappersConfiguration {

	@Bean
	@Primary
	ObjectMapper jsonMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
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
