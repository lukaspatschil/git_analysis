package com.tuwien.gitanalyser.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.TimeZone;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                   .serializationInclusion(JsonInclude.Include.NON_NULL)
                   .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                   .indentOutput(true)
                   .findModulesViaServiceLoader(true)
                   .modules(new JavaTimeModule())
                   .timeZone(TimeZone.getDefault());
    }
}
