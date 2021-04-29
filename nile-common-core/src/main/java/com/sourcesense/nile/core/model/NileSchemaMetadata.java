package com.sourcesense.nile.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
    public static final String KEY_ROOT_COLLECTION = "root_collection";

    private NileURI.Subtype subtype;

    @JsonProperty("uid")
    private String uidKey;

    private String collection;
    private String name;
    private String description;
    private Boolean development = false;

    @JsonProperty(KEY_ROOT_QUERY)
    private JsonNode rootQuery;

    @JsonProperty(KEY_ROOT_COLLECTION)
    private String rootCollection;

    private NileURI parent;

    public NileSchemaMetadata validate() throws InvalidMetadataException {
        if(name == null){
            throw new InvalidMetadataException("Missing [name] from metadata");
        }

        if(subtype == null){
            throw new InvalidMetadataException("Missing [subtype] from metadata");
        }

        if(parent != null){
          return this;
        }

        if(uidKey == null){
            throw new InvalidMetadataException("Missing [uid] from metadata");
        }

        if(collection == null){
            throw new InvalidMetadataException("Missing [collection] from metadata");
        }

        switch (subtype){
            case MODEL:
                if(rootCollection == null){
                    throw new InvalidMetadataException("Missing [root_collection] from metadata");
                }
                if(rootQuery == null){
                    throw new InvalidMetadataException("Missing [root_query] from metadata");
                }
                break;
            case IMPORT:
                break;
        }
        return this;
    }

}
