package com.sourcesense.nile.connectorcore.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.core.enumeration.IngestionAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DataEntry {

    private String nileUri;
    private String schemaKey;
    private JsonNode data;
    private IngestionAction action;
}
