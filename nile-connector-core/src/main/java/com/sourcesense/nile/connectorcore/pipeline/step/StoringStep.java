package com.sourcesense.nile.connectorcore.pipeline.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.connectorcore.model.DataEntry;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.service.RawDataMessageService;
import com.sourcesense.nile.core.errors.PipePanic;
import com.sourcesense.nile.core.pipeline.Step;
import com.sourcesense.nile.core.pipeline.step.AbstractStep;
import com.sourcesense.nile.core.pipeline.step.StepPayload;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
//                .map(Observable::fromFuture)

//        List<SendResult<String, JsonNode>> sendResults = Collections.synchronizedList(new ArrayList<>());
//
//        List<Observable<SendResult<String, JsonNode>>> messageObservables = input.getLoad().stream()
//                .map(rawDataMessageService::sendMessageToOutputTopic)
//                .map(ListenableFuture::completable)
//                .map(Observable::fromFuture)
//                .collect(Collectors.toList());
//
//        Observable.concat(messageObservables)
//                .doOnNext(sendResults::add)
//                .subscribe();

//        return input.buildNextStepPayload(sendResults)z;
    }
}
