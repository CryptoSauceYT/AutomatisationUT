package io.sauce.tradingbot.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (bigDecimal != null) {
            String value = bigDecimal.stripTrailingZeros().toPlainString();
            jsonGenerator.writeString(value);
        } else {
            jsonGenerator.writeNull();
        }
    }

    @Override
    public Class<BigDecimal> handledType() {
        return BigDecimal.class;
    }
}
