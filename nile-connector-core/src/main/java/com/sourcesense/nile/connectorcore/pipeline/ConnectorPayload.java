package com.sourcesense.nile.connectorcore.pipeline;

import com.sourcesense.nile.core.pipeline.GenericPayload;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConnectorPayload<T> extends GenericPayload<T> {

    public ConnectorPayload(T data) {
        super(data);
    }
}
