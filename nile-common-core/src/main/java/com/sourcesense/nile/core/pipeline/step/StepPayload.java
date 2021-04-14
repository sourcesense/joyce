package com.sourcesense.nile.core.pipeline.step;

import com.sourcesense.nile.core.pipeline.GenericPayload;
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
}
