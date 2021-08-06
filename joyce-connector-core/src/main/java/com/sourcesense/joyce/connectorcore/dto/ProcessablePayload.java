package com.sourcesense.joyce.connectorcore.dto;

import com.sourcesense.joyce.connectorcore.model.DataInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessablePayload<I extends DataInfo, L> {

    protected List<I> dataInfoList;
    protected List<L> stepLoadList;

}
