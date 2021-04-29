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

package com.sourcesense.nile.connectorcore.scheduling.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.connectorcore.service.DataProcessingService;
import io.reactivex.Observable;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class ScanningJob<R extends MappingInfo, P extends ProcessableData> implements Job {

    private final DataProcessingService<R, P> dataProcessingService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        List<Observable<SendResult<String, JsonNode>>> messageObservables = dataProcessingService
                .execute(jobExecutionContext).stream()
                .map(Observable::fromFuture)
                .collect(Collectors.toList());

        Observable.concat(messageObservables).subscribe();
    }
}
