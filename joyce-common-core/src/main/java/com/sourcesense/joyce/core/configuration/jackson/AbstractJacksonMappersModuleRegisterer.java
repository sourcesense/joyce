package com.sourcesense.joyce.core.configuration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.sourcesense.joyce.core.mapping.jackson.deserializer.JoyceURIDeserializer;
import com.sourcesense.joyce.core.mapping.jackson.serializer.JoyceURISerializer;
import com.sourcesense.joyce.core.model.uri.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractJacksonMappersModuleRegisterer {

	protected final JoyceURIFactory joyceURIFactory;
	protected final ObjectMapper jsonMapper;
	protected final YAMLMapper yamlMapper;

	protected abstract void registerAdditionalJoyceURIDeserializer(SimpleModule joyceURIModule);

	public void registerJoyceURIModule() {
		SimpleModule joyceURIModule = new SimpleModule();

		joyceURIModule.addSerializer(JoyceURI.class, new JoyceURISerializer());

		joyceURIModule.addDeserializer(JoyceURI.class, new JoyceURIDeserializer<>(joyceURIFactory, JoyceURI.class));
		joyceURIModule.addDeserializer(JoyceTaxonomyURI.class, new JoyceURIDeserializer<>(joyceURIFactory, JoyceTaxonomyURI.class));
		joyceURIModule.addDeserializer(JoyceContentURI.class, new JoyceURIDeserializer<>(joyceURIFactory, JoyceContentURI.class));
		joyceURIModule.addDeserializer(JoyceSchemaURI.class, new JoyceURIDeserializer<>(joyceURIFactory, JoyceSchemaURI.class));
		joyceURIModule.addDeserializer(JoyceSourceURI.class, new JoyceURIDeserializer<>(joyceURIFactory, JoyceSourceURI.class));
		joyceURIModule.addDeserializer(JoyceDocumentURI.class, new JoyceURIDeserializer<>(joyceURIFactory, JoyceDocumentURI.class));

		this.registerAdditionalJoyceURIDeserializer(joyceURIModule);

		jsonMapper.registerModule(joyceURIModule);
		yamlMapper.registerModule(joyceURIModule);
	}
}
