package com.sourcesense.nile.connectorcore.model;

import com.sourcesense.nile.core.enumeration.IngestionAction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ProcessableData {

    protected IngestionAction action;
    protected String schemaKey;
}
