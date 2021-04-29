package com.sourcesense.nile.connectorcore.model.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingApiResponseRow {

    private String nileUri;
    private JsonNode content;
}
