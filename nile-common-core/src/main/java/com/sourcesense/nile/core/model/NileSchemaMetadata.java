package com.sourcesense.nile.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.errors.InvalidMetadataException;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class NileSchemaMetadata {
    /**
     * key Constants
     */
    public static final String KEY_ROOT_QUERY = "root_query";
    public static final String KEY_COLLECTION = "collection";
    public static final String KEY_UID = "uid";
    public static final String KEY_TYPE = "type";

    private NileURI.Subtype subtype;
    private String uidKey;
    private String collection;
    private JsonNode rootQuery;

    public static NileSchemaMetadata create(JsonNode schema) {
        if (schema == null) {
            throw new InvalidMetadataException("Missing metadata");
        }

        NileSchemaMetadata metadata = new NileSchemaMetadata();

        metadata.setCollection(Optional.ofNullable(schema.get(KEY_COLLECTION))
                .orElseThrow(() -> new InvalidMetadataException(
                        String.format("Missing [%s] in metadata", KEY_COLLECTION))).asText());

        metadata.setUidKey(Optional.ofNullable(schema.get(KEY_UID))
                .orElseThrow(() -> new InvalidMetadataException(
                        String.format("Missing [%s] in metadata", KEY_UID))).asText());


        String subtype = Optional.ofNullable(schema.get(KEY_TYPE))
                .orElseThrow(() -> new InvalidMetadataException(
                        String.format("Missing [%s] in metadata", KEY_TYPE))).asText();

        metadata.setSubtype(NileURI.Subtype.get(subtype).orElseThrow(() -> new InvalidMetadataException(
                String.format("Invalid value for Type [%s]", subtype))));

        //TODO: optional further constraints if type is import or model

        switch (metadata.getSubtype()){
            case MODEL:
                metadata.setRootQuery(Optional.ofNullable(schema.get(KEY_ROOT_QUERY))
                        .orElseThrow(() -> new InvalidMetadataException(
                                String.format("[%s] is mandatory for [%s] type", KEY_ROOT_QUERY, metadata.getSubtype().getValue()))));
            case IMPORT:
        }

        return metadata;
    }
}
