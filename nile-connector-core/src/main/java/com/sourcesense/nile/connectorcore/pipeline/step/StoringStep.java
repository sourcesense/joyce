package com.sourcesense.nile.connectorcore.pipeline.step;

import com.sourcesense.nile.connectorcore.model.DataEntry;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.core.errors.PipePanic;
import com.sourcesense.nile.core.pipeline.Step;
import com.sourcesense.nile.core.pipeline.step.AbstractStep;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoringStep<R extends MappingInfo>
        extends AbstractStep<DataEntry, Void>
        implements Step<StepPayload<List<R>, List<DataEntry>>, StepPayload<List<R>, Void>> {

    @Override
    public StepPayload<List<R>, Void> process(StepPayload<List<R>, List<DataEntry>> input) throws PipePanic {
        return null;
    }
}
