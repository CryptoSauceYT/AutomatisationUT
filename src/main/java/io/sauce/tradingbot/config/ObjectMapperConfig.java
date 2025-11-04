package io.sauce.tradingbot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.sauce.tradingbot.util.JSONUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder){
        jackson2ObjectMapperBuilder
                .serializerByType(BigDecimal.class,new BigDecimalSerializer());
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.build();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.registerModule(new JavaTimeModule());
        JSONUtil.setObjectMapper(objectMapper);
        return objectMapper;
    }

}
