package com.sourcesense.nile.core.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class Notification {
    private String source;
    private String event;
    private String correlation_uid;
    private Map metadata;
}
