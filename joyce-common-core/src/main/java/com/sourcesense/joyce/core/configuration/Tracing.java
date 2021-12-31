package com.sourcesense.joyce.core.configuration;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class Tracing {

	private final Tracer tracer;

	@PostConstruct
	public void registerToGlobalTracer() {
		if (!GlobalTracer.isRegistered()) {
			GlobalTracer.registerIfAbsent(tracer);
		}
	}
}
