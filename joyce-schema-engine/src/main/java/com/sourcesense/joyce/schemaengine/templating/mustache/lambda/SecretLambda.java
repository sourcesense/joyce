package com.sourcesense.joyce.schemaengine.templating.mustache.lambda;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.sourcesense.joyce.schemaengine.annotation.MustacheLambda;
import com.sourcesense.joyce.schemaengine.service.CryptingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;

@Component
@RequiredArgsConstructor
@MustacheLambda(tag = "secret")
public class SecretLambda implements Mustache.Lambda {

	private final CryptingService cryptingService;

    @Override
    public void execute(Template.Fragment fragment, Writer writer) throws IOException {
        writer.append(
						cryptingService.decrypt(fragment.execute())
				);
    }
}
