package com.sourcesense.nile.connectorcore.model;

import com.sourcesense.nile.core.enumeration.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ProcessableData {

    protected Action action;
    protected String schemaKey;
}
