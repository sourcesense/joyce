package com.sourcesense.nile.connectorcore.pipeline.step;

import com.sourcesense.nile.core.errors.PipePanic;
import com.sourcesense.nile.core.pipeline.Step;
import com.sourcesense.nile.core.pipeline.step.AbstractStep;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoringStep
        extends AbstractStep<String, String>
        implements Step<StepPayload<?, ?>, StepPayload<?, ?>> {

    @Override
    public StepPayload<?, ?> process(StepPayload<?, ?> input) throws PipePanic {
        return null;
    }
}
