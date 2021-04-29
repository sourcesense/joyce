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

package com.sourcesense.nile.connectorcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.connectorcore.pipeline.step.ExtractionStep;
import com.sourcesense.nile.connectorcore.pipeline.step.ReadingStep;
import com.sourcesense.nile.connectorcore.pipeline.step.StoringStep;
import com.sourcesense.nile.core.exceptions.PipePanic;
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
            //Todo: handle properly exceptions
            throw new RuntimeException(pipePanic);
//            notificationService.ko("", pipePanic.getEvent(), pipePanic.getMessage());
        }
    }
}
