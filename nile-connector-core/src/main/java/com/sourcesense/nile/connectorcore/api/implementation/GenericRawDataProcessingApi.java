package com.sourcesense.nile.connectorcore.api.implementation;

import com.sourcesense.nile.connectorcore.api.RawDataProcessingApi;
import com.sourcesense.nile.connectorcore.model.DataInfo;
import com.sourcesense.nile.connectorcore.dto.ProcessableData;
import com.sourcesense.nile.connectorcore.dto.ProcessingApiResponse;
import com.sourcesense.nile.connectorcore.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
public abstract class GenericRawDataProcessingApi<I extends DataInfo, D extends ProcessableData> implements RawDataProcessingApi {

    private final DataProcessingService<I, D> dataProcessingService;

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
