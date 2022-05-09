package com.sourcesense.joyce.core.configuration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;

public class JacksonMappersModuleRegisterer extends AbstractJacksonMappersModuleRegisterer {

	public JacksonMappersModuleRegisterer(ObjectMapper jsonMapper, YAMLMapper yamlMapper) {
		super(JoyceURIFactory.getInstance(), jsonMapper, yamlMapper);
	}

	@Override
	protected void registerAdditionalJoyceURIDeserializer(SimpleModule joyceURIModule) {

	}
}
