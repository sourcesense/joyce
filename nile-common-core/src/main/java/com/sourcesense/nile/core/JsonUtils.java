package com.sourcesense.nile.core;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class JsonUtils {
    public static Stream<Map.Entry<String, JsonNode>> fieldsStream(JsonNode json){
        if (json == null){
            return Stream.empty();
        }
        Iterator<Map.Entry<String, JsonNode>> asd = json.fields();
        return StreamSupport.stream( Spliterators.spliteratorUnknownSize(json.fields(), Spliterator.ORDERED), false);
    }
}
