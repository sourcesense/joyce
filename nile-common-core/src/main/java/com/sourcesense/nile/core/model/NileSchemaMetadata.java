package com.sourcesense.nile.core.model;

import com.fasterxml.jackson.databind.JsonNode;
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
    public static final String KEY_COLLECTION = "collection";
    public static final String KEY_UID = "uid";
    public static final String KEY_SUBTYPE = "subtype";
    public static final String KEY_ROOT_QUERY = "root_query";
    public static final String KEY_ROOT_COLLECTION = "root_collection";

    private NileURI.Subtype subtype;
    private String uidKey;
    private String collection;
    private JsonNode rootQuery;
    private String rootCollection;

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


        String subtype = Optional.ofNullable(schema.get(KEY_SUBTYPE))
                .orElseThrow(() -> new InvalidMetadataException(
                        String.format("Missing [%s] in metadata", KEY_SUBTYPE))).asText();

        metadata.setSubtype(NileURI.Subtype.get(subtype).orElseThrow(() -> new InvalidMetadataException(
                String.format("Invalid value for Type [%s]", subtype))));

        switch (metadata.getSubtype()){
            case MODEL:
                metadata.setRootQuery(Optional.ofNullable(schema.get(KEY_ROOT_QUERY))
                        .orElseThrow(() -> new InvalidMetadataException(
                                String.format("[%s] is mandatory for [%s] type", KEY_ROOT_QUERY, metadata.getSubtype().getValue()))));
                metadata.setRootCollection(Optional.ofNullable(schema.get(KEY_ROOT_COLLECTION))
                        .orElseThrow(() -> new InvalidMetadataException(
                                String.format("[%s] is mandatory for [%s] type", KEY_ROOT_COLLECTION, metadata.getSubtype().getValue()))).asText());
            case IMPORT:
                //TODO:
        }

        return metadata;
    }
}
