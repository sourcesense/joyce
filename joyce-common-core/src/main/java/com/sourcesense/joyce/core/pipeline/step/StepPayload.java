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

package com.sourcesense.joyce.core.pipeline.step;

import com.sourcesense.joyce.core.pipeline.GenericPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepPayload<R, L> {

    // collettore variabili ricevute in input
    private GenericPayload<R> initialPayload;

    //l'output dello step diventa input dello step successivo
    private L load;

    // ctor
    public StepPayload(StepPayload<R, L> stepPayload) {
        if (Objects.nonNull(stepPayload)) {
            this.initialPayload = stepPayload.getInitialPayload();
            this.load = stepPayload.getLoad();
        }
    }

    public <N> StepPayload<R, N> buildNextStepPayload(N processedLoad) {
        return new StepPayload<>(
                this.initialPayload,
                processedLoad
        );
    }
}
