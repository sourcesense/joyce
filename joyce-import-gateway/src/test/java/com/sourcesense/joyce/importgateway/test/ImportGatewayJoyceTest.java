package com.sourcesense.joyce.importgateway.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.sourcesense.joyce.core.configuration.jackson.JacksonMappersModuleRegisterer;
import com.sourcesense.joyce.test.utility.JoyceTest;

public abstract class ImportGatewayJoyceTest extends JoyceTest {

	@Override
	protected void setupMappers(CsvMapper csvMapper, YAMLMapper yamlMapper, ObjectMapper jsonMapper) {
		JacksonMappersModuleRegisterer moduleRegisterer = new JacksonMappersModuleRegisterer(jsonMapper, yamlMapper);
		moduleRegisterer.registerJoyceURIModule();
	}
}
