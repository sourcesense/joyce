package com.sourcesense.nile.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties("nile.notification-service")
public class NotificationServiceProperties {
    /**
     * Service is enabled or not
     */
    private Boolean enabled;

    /**
     * Notification topic
     */
    private String topic;
}
