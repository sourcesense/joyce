package com.sourcesense.nile.connectorcore.dao;

import com.sourcesense.nile.connectorcore.dto.DataInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

public interface ConnectorDao<T extends DataInfo> {

    String INSERT_DATE = "insertDate";

    Optional<T> get(String id);
    List<T> getAll();
    T save(T dataInfo);
    List<T> saveAll(List<T> dataInfos);
    void delete(T dataInfo);
}
