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

package com.sourcesense.nile.connectorcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.connectorcore.dto.DataEntry;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.service.KafkaMessageService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Log4j2
@Service
public class RawDataMessageService extends KafkaMessageService<JsonNode> {

    @Value("${nile.kafka.import-topic}")
    private String importTopic;

    public RawDataMessageService(
            ObjectMapper jsonMapper,
            KafkaTemplate<String, JsonNode> kafkaTemplate) {

        super(jsonMapper, kafkaTemplate);
    }

    public ListenableFuture<SendResult<String, JsonNode>> sendMessageToOutputTopic(DataEntry dataEntry) {
        Message<JsonNode> rawDataMessage = this.getRawDataMessage(dataEntry);
        return this.sendMessage(dataEntry.getNileUri(), StringUtils.EMPTY, rawDataMessage);
    }

    private Message<JsonNode> getRawDataMessage(DataEntry entry) {
        return MessageBuilder
                .withPayload(entry.getData())
                .setHeader(KafkaHeaders.TOPIC, importTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, entry.getNileUri())
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, entry.getAction().toString())
                .setHeader(KafkaCustomHeaders.IMPORT_SCHEMA, entry.getSchemaKey())
                .build();
    }

    @Override
    public void handleMessageSuccess(Message<JsonNode> message, SendResult<String, JsonNode> result, String rawUri, String contentUri, JsonNode eventPayload, JsonNode eventMetadata) {
        if (log.isDebugEnabled()) {
            log.debug("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
        }
    }

    @Override
    public void handleMessageFailure(Message<JsonNode> message, Throwable throwable, String rawUri, String contentUri, JsonNode eventPayload, JsonNode eventMetadata) {
        log.error("Unable to send message=[{}] due to : [{}]", message, throwable.getMessage());
    }
}
