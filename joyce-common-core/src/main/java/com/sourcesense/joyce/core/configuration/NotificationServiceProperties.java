/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties("joyce.notification-service")
public class NotificationServiceProperties {
    /**
     * Service is enabled or not
     */
    private Boolean enabled;

    /**
     * Notification topic
     */
    private String topic = "joyce_notification";

    /**
     * Notification topic partitions
     */
    private Integer partitions = 10;

    /**
     * Notification topic replicas
     */
    private Integer replicas = 1;

    /**
     * Notification topic retention in milliseconds
     */
    private Integer retention = 259200000; // 3 days

    /**
     * Notification Source, the software object that generated the notification
     */
    public String source;
}