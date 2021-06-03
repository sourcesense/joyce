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

package com.sourcesense.joyce.connectorcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.connectorcore.dto.ProcessableData;
import com.sourcesense.joyce.connectorcore.pipeline.step.StoringStep;
import com.sourcesense.joyce.connectorcore.model.DataInfo;
import com.sourcesense.joyce.connectorcore.pipeline.step.ExtractionStep;
import com.sourcesense.joyce.connectorcore.pipeline.step.ReadingStep;
import com.sourcesense.joyce.core.exception.PipePanic;
import com.sourcesense.joyce.core.exception.ProcessingException;
import com.sourcesense.joyce.core.pipeline.Pipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class DataProcessingService<I extends DataInfo, D extends ProcessableData> {

    private final ReadingStep<I, D> readingStep;
    private final ExtractionStep<I, D> extractionStep;
    private final StoringStep<I> storingStep;

    public List<CompletableFuture<SendResult<String, JsonNode>>> execute() {
        try {
            return new Pipeline<>(readingStep)
                    .pipe(extractionStep)
                    .pipe(storingStep)
                    .execute(Optional.empty());

        } catch (PipePanic pipePanic) {
            throw ProcessingException.builder()
                    .message(pipePanic.getMessage())
                    .event(pipePanic.getEvent())
                    .build();
        }
    }
}
