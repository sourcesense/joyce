package com.sourcesense.nile.connectorcore.pipeline.step;

import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.core.pipeline.Step;
import com.sourcesense.nile.core.pipeline.step.AbstractStep;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public abstract class ReadingStep<R extends MappingInfo, O extends ProcessableData>
        extends AbstractStep<Void, List<R>>
        implements Step<StepPayload<Void, Void>, StepPayload<List<R>, List<O>>> {

}
