package com.sourcesense.nile.core.pipeline.step;

import com.sourcesense.nile.core.exceptions.PipePanic;
import com.sourcesense.nile.core.utililty.TextUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Log4j2
public abstract class AbstractStep<R, L> {


    protected StepPayload<R, L> getNextStepPayload(StepPayload<R, L> input) {
        return new StepPayload<>(input.getInitialPayload(), input.getLoad());
    }

    /**
     * Utility method to create a PipePanic blocking error
     *
     * @param exception Blocking Exception
     * @return PipePanic
     */
    protected PipePanic getUnrecoverableStateException(Exception exception) {
        log.error("Message: {}. Stack Trace: {}", exception.getMessage(), ExceptionUtils.getStackTrace(exception));
        return new PipePanic("Message: '" + TextUtils.limitMessageSize(exception.getMessage()) + "'");
    }

    /**
     * Utility method to copy common fields between two steps
     *
     * @param input Current pipeline step input
     * @param output Current pipeline step output
     * @param <I> Input class
     * @param <O> Output class
     */
    protected <I, O, K> void copyStepPayloadCommonFields(
            StepPayload<K, I> input,
            StepPayload<K, O> output) {

        output.setInitialPayload(input.getInitialPayload());
    }
}
