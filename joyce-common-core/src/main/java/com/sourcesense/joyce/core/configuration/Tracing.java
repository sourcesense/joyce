package com.sourcesense.joyce.core.configuration;

import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class Tracing {

	@Autowired
	Tracer tracer;

	@Value("${opentracing.jaeger.service-name}")
	String name;

	@PostConstruct
	public void registerToGlobalTracer() {
		if (!GlobalTracer.isRegistered()) {
			GlobalTracer.registerIfAbsent(tracer);
		}
	}


}
