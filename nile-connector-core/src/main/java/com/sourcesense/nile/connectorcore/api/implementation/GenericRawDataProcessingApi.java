package com.sourcesense.nile.connectorcore.api.implementation;

import com.sourcesense.nile.connectorcore.api.RawDataProcessingApi;
import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.connectorcore.model.response.ProcessingApiResponse;
import com.sourcesense.nile.connectorcore.model.response.ProcessingApiResponseRow;
import com.sourcesense.nile.connectorcore.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public abstract class GenericRawDataProcessingApi<R extends MappingInfo, P extends ProcessableData> implements RawDataProcessingApi {

    private final DataProcessingService<R, P> dataProcessingService;

    @Override
    public ProcessingApiResponse process() {
        List<ProcessingApiResponseRow> rows = dataProcessingService.execute().stream()
                .map(CompletableFuture::join)
                .map(SendResult::getProducerRecord)
                .map(record -> ProcessingApiResponseRow.builder()
                        .nileUri(record.key())
                        .content(record.value())
                        .build())
                .collect(Collectors.toList());

        return new ProcessingApiResponse(rows);
    }
}
