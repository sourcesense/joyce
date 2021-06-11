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

package com.sourcesense.joyce.connectorcore.scheduling.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.connectorcore.dto.ProcessableData;
import com.sourcesense.joyce.connectorcore.exception.handler.JobExceptionHandler;
import com.sourcesense.joyce.connectorcore.model.DataInfo;
import com.sourcesense.joyce.connectorcore.service.DataProcessingService;
import io.reactivex.Observable;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class ScanningJob<I extends DataInfo, D extends ProcessableData>
        implements Job {

    private final JobExceptionHandler jobExceptionHandler;
    private final DataProcessingService<I, D> dataProcessingService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            List<Observable<SendResult<String, JsonNode>>> messageObservables = dataProcessingService
                    .execute().stream()
                    .map(Observable::fromFuture)
                    .collect(Collectors.toList());

            Observable.concat(messageObservables).subscribe();

        } catch (Exception exception) {
            jobExceptionHandler.handleGenericException(exception);
        }
    }
}