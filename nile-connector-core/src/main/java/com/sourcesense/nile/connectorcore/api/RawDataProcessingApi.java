package com.sourcesense.nile.connectorcore.api;

import com.sourcesense.nile.connectorcore.dto.ProcessingApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("api/raw/process")
@Tag(name = "Raw Data Processing API", description = "Raw Data Processing API")
public interface RawDataProcessingApi {

    @PostMapping(produces = "application/json; charset=utf-8")
    @ResponseStatus(code = HttpStatus.CREATED)
    ProcessingApiResponse process();

}
