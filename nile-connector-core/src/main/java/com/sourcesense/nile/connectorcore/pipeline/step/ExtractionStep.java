package com.sourcesense.nile.connectorcore.pipeline.step;

import com.sourcesense.nile.connectorcore.model.DataEntry;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.core.pipeline.Step;
import com.sourcesense.nile.core.pipeline.step.AbstractStep;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public abstract class ExtractionStep<R extends MappingInfo, I extends ProcessableData>
        extends AbstractStep<List<I>, List<DataEntry>>
        implements Step<StepPayload<List<R>, List<I>>, StepPayload<List<R>, List<DataEntry>>> {

}
