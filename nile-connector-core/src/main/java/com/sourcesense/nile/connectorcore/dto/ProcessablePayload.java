package com.sourcesense.nile.connectorcore.dto;

import com.sourcesense.nile.connectorcore.model.ProcessableData;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
public class ProcessablePayload<T extends DataInfo, R extends ProcessableData> {

    private List<T> dataInfoList;
    private List<R> processableDataList;

}
