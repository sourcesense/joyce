package com.sourcesense.nile.connectorcore.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nile.data.collection")
public class DatabaseCollections {

    private String dataInfo;
}
