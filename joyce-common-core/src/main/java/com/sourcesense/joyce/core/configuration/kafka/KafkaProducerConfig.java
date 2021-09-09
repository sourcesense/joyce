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

package com.sourcesense.joyce.core.configuration.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingConsumerInterceptor;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.contrib.kafka.spring.TracingProducerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(value = "joyce.kafka.producer.enabled", havingValue = "true")
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaProducerConfig {
		final private Tracer tracer;
    @Value("${joyce.kafka.bootstrapAddress}")
    String bootstrapAddress;

    @Bean
    @Scope("prototype")
    public ProducerFactory<String, JsonNode> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
				configProps.put(
					ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
					bootstrapAddress);
        configProps.put(
                ProducerConfig.MAX_REQUEST_SIZE_CONFIG,
						2097152);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
				//configProps.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,  Collections.singletonList(TracingProducerInterceptor.class));

			return new TracingProducerFactory<>(new DefaultKafkaProducerFactory<>(configProps), tracer);
    }

    @Bean
    @Scope("prototype")
    public KafkaTemplate<String, JsonNode> kafkaTemplate(ProducerFactory<String, JsonNode> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
