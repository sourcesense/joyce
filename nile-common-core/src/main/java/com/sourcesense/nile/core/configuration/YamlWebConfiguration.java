package com.sourcesense.nile.core.configuration;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class YamlWebConfiguration implements WebMvcConfigurer {

    final class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        YamlJackson2HttpMessageConverter() {
            super(new YAMLMapper(), MediaType.parseMediaType("application/x-yaml"));
        }
    }


    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        converters.add(new YamlJackson2HttpMessageConverter());
    }
}
