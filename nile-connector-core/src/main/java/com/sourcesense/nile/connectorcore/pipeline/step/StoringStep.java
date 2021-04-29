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

package com.sourcesense.nile.connectorcore.pipeline.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.connectorcore.model.DataEntry;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.service.RawDataMessageService;
import com.sourcesense.nile.core.exceptions.PipePanic;
import com.sourcesense.nile.core.pipeline.Step;
import com.sourcesense.nile.core.pipeline.step.AbstractStep;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class StoringStep<R extends MappingInfo>
        extends AbstractStep<DataEntry, List<CompletableFuture<SendResult<String, JsonNode>>>>
        implements Step<StepPayload<List<R>, List<DataEntry>>, StepPayload<List<R>, List<CompletableFuture<SendResult<String, JsonNode>>>>> {

    private final RawDataMessageService rawDataMessageService;

    @Override
    public StepPayload<List<R>, List<CompletableFuture<SendResult<String, JsonNode>>>> process(StepPayload<List<R>, List<DataEntry>> input) throws PipePanic {

        List<CompletableFuture<SendResult<String, JsonNode>>> messageFutures = input.getLoad().stream()
                .map(rawDataMessageService::sendMessageToOutputTopic)
                .map(ListenableFuture::completable)
                .collect(Collectors.toList());

        return input.buildNextStepPayload(messageFutures);
    }
}
