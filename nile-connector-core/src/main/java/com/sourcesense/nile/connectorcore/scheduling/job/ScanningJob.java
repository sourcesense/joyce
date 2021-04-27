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
