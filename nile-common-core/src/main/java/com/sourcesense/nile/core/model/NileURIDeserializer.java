package com.sourcesense.nile.core.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Optional;

public class NileURIDeserializer extends StdDeserializer<NileURI> {
    protected NileURIDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public NileURI deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Optional<NileURI> uri = NileURI.createURI(node.asText());
        if (uri.isEmpty()){
            throw new JsonProcessingException(String.format("uri: %s is not a nile uri", node.asText())){};
        }
        return uri.get();
    }
}
