package com.sourcesense.nile.core.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class NileURISerializer extends StdSerializer<NileURI> {
    protected NileURISerializer() {
        this(null);
    }
    protected NileURISerializer(Class<NileURI> t) {
        super(t);
    }

    @Override
    public void serialize(NileURI uri, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(uri.toString());
    }
}
