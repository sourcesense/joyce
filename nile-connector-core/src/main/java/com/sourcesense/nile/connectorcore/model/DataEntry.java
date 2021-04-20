package com.sourcesense.nile.connectorcore.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.core.enumeration.Action;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DataEntry {

    private String schemaKey;
    private String uid;
    private JsonNode data;
    private Action action;
}
