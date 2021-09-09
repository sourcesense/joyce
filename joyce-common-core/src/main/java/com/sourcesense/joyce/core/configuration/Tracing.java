package com.sourcesense.joyce.core.configuration;

import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class Tracing {


	@Value("${opentracing.jaeger.service-name}")
	String name;

	@PostConstruct
	public void registerToGlobalTracer() {
		if (!GlobalTracer.isRegistered()) {
			GlobalTracer.registerIfAbsent(tracer());
		}
	}

	@Bean
	public Tracer tracer() {
		return io.jaegertracing.Configuration.fromEnv(name)
				.withSampler(
						io.jaegertracing.Configuration.SamplerConfiguration.fromEnv()
								.withType(ConstSampler.TYPE)
								.withParam(1))
				.withReporter(
						io.jaegertracing.Configuration.ReporterConfiguration.fromEnv()
								.withLogSpans(true)
								.withFlushInterval(1000)
								.withMaxQueueSize(10000)
								.withSender(
										io.jaegertracing.Configuration.SenderConfiguration.fromEnv()
												.withAgentHost("localhost")
												.withAgentPort(6831)
								))
				.getTracer();
	}

}
