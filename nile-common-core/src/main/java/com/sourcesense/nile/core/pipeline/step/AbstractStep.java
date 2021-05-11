/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.nile.core.pipeline.step;

import com.sourcesense.nile.core.exception.PipePanic;
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
