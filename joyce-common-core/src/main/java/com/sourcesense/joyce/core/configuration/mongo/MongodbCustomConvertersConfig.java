package com.sourcesense.joyce.core.configuration.mongo;

import com.sourcesense.joyce.core.mapping.mongo.JoyceURIMongoConverters;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongodbCustomConvertersConfig extends AbstractMongodbCustomConvertersConfig{

	public MongodbCustomConvertersConfig() {
		super(JoyceURIFactory.getInstance());
	}

	@Bean
	@ConditionalOnMissingBean(JoyceURIMongoConverters.class)
	public JoyceURIMongoConverters joyceURIMongoConverters() {
		return JoyceURIMongoConverters.from(
				this.computeDefaultJoyceURIMongoConverters()
		);
	}
}
