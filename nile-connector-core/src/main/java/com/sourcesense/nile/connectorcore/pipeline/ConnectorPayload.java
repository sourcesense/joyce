package com.sourcesense.nile.connectorcore.pipeline;

import com.sourcesense.nile.core.pipeline.GenericPayload;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConnectorPayload<T> extends GenericPayload<T> {

}
