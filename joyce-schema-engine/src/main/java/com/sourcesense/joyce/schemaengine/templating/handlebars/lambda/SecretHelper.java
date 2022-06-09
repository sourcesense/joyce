package com.sourcesense.joyce.schemaengine.templating.handlebars.lambda;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.sourcesense.joyce.schemaengine.annotation.HandlebarsHelper;
import com.sourcesense.joyce.schemaengine.service.CryptingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@HandlebarsHelper(tag = "secret")
public class SecretHelper implements Helper<Map<String, Object>> {

	private final CryptingService cryptingService;

	@Override
	public Object apply(Map<String, Object> context, Options options) throws IOException {
		String secret = options.fn(context).toString();
		return cryptingService.decrypt(secret);
	}
}
