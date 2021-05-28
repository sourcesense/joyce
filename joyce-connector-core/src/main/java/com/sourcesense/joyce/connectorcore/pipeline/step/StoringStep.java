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

package com.sourcesense.joyce.connectorcore.pipeline.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.connectorcore.dao.ConnectorDao;
import com.sourcesense.joyce.connectorcore.service.RawDataMessageService;
import com.sourcesense.joyce.connectorcore.model.DataInfo;
import com.sourcesense.joyce.connectorcore.dto.ProcessablePayload;
import com.sourcesense.joyce.connectorcore.dto.DataEntry;
import com.sourcesense.joyce.core.exception.PipePanic;
import com.sourcesense.joyce.core.pipeline.Step;
import com.sourcesense.joyce.core.pipeline.step.AbstractStep;
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
public class StoringStep<I extends DataInfo>
        extends AbstractStep<ProcessablePayload<I, DataEntry>, List<CompletableFuture<SendResult<String, JsonNode>>>>
        implements Step<ProcessablePayload<I, DataEntry>, List<CompletableFuture<SendResult<String, JsonNode>>>> {

    private final ConnectorDao<I> connectorDao;
    private final RawDataMessageService rawDataMessageService;

    @Override
    public List<CompletableFuture<SendResult<String, JsonNode>>> process(ProcessablePayload<I, DataEntry> input) throws PipePanic {

        connectorDao.saveAll(input.getDataInfoList());
        return input.getStepLoadList().stream()
                .map(rawDataMessageService::sendMessageToOutputTopic)
                .map(ListenableFuture::completable)
                .collect(Collectors.toList());

    }
}
