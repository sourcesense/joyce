/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class JoyceSchemaMetadata {
    /**
     * key Constants
     */
    public static final String KEY_ROOT_QUERY = "root_query";
    public static final String KEY_ROOT_COLLECTION = "root_collection";



    @JsonProperty("uid")
    private String uidKey;
    private JoyceURI.Subtype subtype;
    private String collection;
    private String name;
    private List<Map<String, Object>> indexes;
    private String description;
    private Boolean development = false;
    private Boolean store = true;
    private Boolean validation = true;
    private Boolean indexed = true;

    @JsonProperty(KEY_ROOT_QUERY)
    private JsonNode rootQuery;

    @JsonProperty(KEY_ROOT_COLLECTION)
    private String rootCollection;

    private JoyceURI parent;
    private JsonNode extra;


    /**
     * Validate the object, some keys are required or not given a subtype
     * throws if it fails to validate
     *
     * @return
     * @throws InvalidMetadataException
     */
    public JoyceSchemaMetadata validate() throws InvalidMetadataException {
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
