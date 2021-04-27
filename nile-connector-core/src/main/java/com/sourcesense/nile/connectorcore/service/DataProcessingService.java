package com.sourcesense.nile.connectorcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.connectorcore.pipeline.step.ExtractionStep;
import com.sourcesense.nile.connectorcore.pipeline.step.ReadingStep;
import com.sourcesense.nile.connectorcore.pipeline.step.StoringStep;
import com.sourcesense.nile.core.errors.PipePanic;
import com.sourcesense.nile.core.pipeline.Pipeline;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import com.sourcesense.nile.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DataProcessingService<R extends MappingInfo, P extends ProcessableData> {

    private final ReadingStep<R, P> readingStep;
    private final ExtractionStep<R, P> extractionStep;
    private final StoringStep<R> storingStep;

    private final NotificationService notificationService;

    public List<CompletableFuture<SendResult<String, JsonNode>>> execute(JobExecutionContext jobExecutionContext) {
        try {
            return new Pipeline<>(readingStep)
                    .pipe(extractionStep)
                    .pipe(storingStep)
                    .execute(new StepPayload<>())
                    .getLoad();

        } catch (PipePanic pipePanic) {
            //Todo: Gestire Eccezioni
            throw new RuntimeException(pipePanic);
//            notificationService.ko("", pipePanic.getEvent(), pipePanic.getMessage());
        }
    }
}
