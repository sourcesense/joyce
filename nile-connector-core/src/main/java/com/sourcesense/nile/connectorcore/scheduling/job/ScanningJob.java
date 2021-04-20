package com.sourcesense.nile.connectorcore.scheduling.job;

import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.connectorcore.pipeline.ConnectorPayload;
import com.sourcesense.nile.connectorcore.pipeline.step.ExtractionStep;
import com.sourcesense.nile.connectorcore.pipeline.step.ReadingStep;
import com.sourcesense.nile.connectorcore.pipeline.step.StoringStep;
import com.sourcesense.nile.core.errors.PipePanic;
import com.sourcesense.nile.core.pipeline.Pipeline;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@RequiredArgsConstructor
public abstract class ScanningJob<R extends MappingInfo, P extends ProcessableData> implements Job {

    protected final ReadingStep<R, P> readingStep;
    protected final ExtractionStep<R, P> extractionStep;
    protected final StoringStep<R> storingStep;

    protected void executeJob(JobExecutionContext jobExecutionContext) {
        try {
            StepPayload<Void, Void> emptyStepPayload = new StepPayload<>(new ConnectorPayload<>(), null);

            new Pipeline<>(readingStep)
                    .pipe(extractionStep)
                    .pipe(storingStep)
                    .execute(emptyStepPayload);

        } catch (PipePanic pipePanic) {
            //Todo: Gestire Eccezioni
        }
    }
}
