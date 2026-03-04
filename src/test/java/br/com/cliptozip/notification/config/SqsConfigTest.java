package br.com.cliptozip.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class SqsConfigTest {

    @Test
    void converterShouldUseProvidedObjectMapperAndBeLenientOnContentType() {
        ObjectMapper om = new JacksonConfig().objectMapper();
        MappingJackson2MessageConverter converter = new SqsConfig().mappingJackson2MessageConverter(om);

        assertSame(om, converter.getObjectMapper());
        assertFalse(converter.isStrictContentTypeMatch());
    }
}
