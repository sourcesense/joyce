package com.sourcesense.nile.connectorcore.api.implementation;

import com.sourcesense.nile.connectorcore.api.RawDataProcessingApi;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.connectorcore.model.response.ProcessingApiResponse;
import com.sourcesense.nile.connectorcore.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
public abstract class GenericRawDataProcessingApi<R extends MappingInfo, P extends ProcessableData> implements RawDataProcessingApi {

    private final DataProcessingService<R, P> dataProcessingService;

    @Override
    public ProcessingApiResponse process() {
        Long processedRowCount = dataProcessingService.execute().stream()
                .map(CompletableFuture::join)
                .map(SendResult::getRecordMetadata)
                .filter(RecordMetadata::hasOffset)
                .count();

        return new ProcessingApiResponse(processedRowCount);
    }
}
