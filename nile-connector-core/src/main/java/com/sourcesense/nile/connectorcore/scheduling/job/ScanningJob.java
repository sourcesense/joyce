package com.sourcesense.nile.connectorcore.scheduling.job;

import com.sourcesense.nile.connectorcore.pipeline.ConnectorPayload;
import com.sourcesense.nile.connectorcore.pipeline.step.MappingStep;
import com.sourcesense.nile.connectorcore.pipeline.step.ReadingStep;
import com.sourcesense.nile.connectorcore.pipeline.step.StoringStep;
import com.sourcesense.nile.core.errors.PipePanic;
import com.sourcesense.nile.core.pipeline.Pipeline;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@RequiredArgsConstructor
public abstract class ScanningJob implements Job {

    protected final ReadingStep readingStep;
    protected final MappingStep mappingStep;
    protected final StoringStep storingStep;

    protected void executeJob(JobExecutionContext jobExecutionContext) {
        try {
            ConnectorPayload<Void> initialPayload = new ConnectorPayload<>();
            new Pipeline<>(readingStep)
                    .pipe(mappingStep)
                    .pipe(storingStep)
                    .execute(new StepPayload<>(
                            initialPayload,
                            initialPayload
                    ));
        } catch (PipePanic pipePanic) {
            //Todo: Gestire Eccezioni
        }
    }
}
