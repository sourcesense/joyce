package com.sourcesense.joyce.core.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import com.sourcesense.joyce.core.configuration.jackson.JacksonMappersModuleRegisterer;
import com.sourcesense.joyce.test.utility.JoyceTest;

public abstract class CommonCoreJoyceTest extends JoyceTest {

	@Override
	protected void setupMappers(CsvMapper csvMapper, YAMLMapper yamlMapper, ObjectMapper jsonMapper) {
		JacksonMappersModuleRegisterer moduleRegisterer = new JacksonMappersModuleRegisterer(jsonMapper, yamlMapper);
		moduleRegisterer.registerJoyceURIModule();
	}

	protected Struct computeStruct(String json) throws InvalidProtocolBufferException {
		Struct.Builder structBuilder = Struct.newBuilder();
		JsonFormat.parser().ignoringUnknownFields().merge(json, structBuilder);
		return structBuilder.build();
	}
}
