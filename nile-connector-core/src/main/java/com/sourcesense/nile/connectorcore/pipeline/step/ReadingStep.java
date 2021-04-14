package com.sourcesense.nile.connectorcore.pipeline.step;

import com.sourcesense.nile.core.pipeline.Step;
import com.sourcesense.nile.core.pipeline.step.AbstractStep;
import com.sourcesense.nile.core.pipeline.step.StepPayload;

import java.util.Map;

public abstract class ReadingStep
        extends AbstractStep<Void, Map<String, ?>>
        implements Step<StepPayload<?, ?>, StepPayload<?, ?>> {

}
