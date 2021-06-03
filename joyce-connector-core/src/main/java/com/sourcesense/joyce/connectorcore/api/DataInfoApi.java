package com.sourcesense.joyce.connectorcore.api;

import com.sourcesense.joyce.connectorcore.model.DataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "api/raw/info")
@Tag(name = "Raw Data Info API", description = "Raw Data Info Management API")
public interface DataInfoApi<T extends DataInfo> {

    @GetMapping(value = "/id/{_id}", produces = "application/json; charset=utf-8")
    @ResponseStatus(code = HttpStatus.OK)
    T getInfoBy_id(@PathVariable String _id);

    @GetMapping(produces = "application/json; charset=utf-8")
    @ResponseStatus(code = HttpStatus.OK)
    List<T> getAllInfo();

    @DeleteMapping("/id/{_id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deleteInfoBy_id(@PathVariable String _id);

}
