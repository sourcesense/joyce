package com.sourcesense.joyce.sink.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.sourcesense.joyce"})
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class MongodbSinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongodbSinkApplication.class, args);
	}

}
