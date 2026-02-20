package br.com.cliptozip.notification.config;

import br.com.cliptozip.notification.domain.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JacksonConfigTest {

    @Test
    void objectMapperShouldIgnoreUnknownProperties() throws Exception {
        ObjectMapper om = new JacksonConfig().objectMapper();

        String json = "{" +
                "\"titulo\":\"t\"," +
                "\"status\":\"s\"," +
                "\"mensagem\":\"m\"," +
                "\"emailUsuario\":\"user@x.com\"," +
                "\"nomeUsuario\":\"n\"," +
                "\"extra\":\"ignored\"" +
                "}";

        NotificationEvent event = om.readValue(json, NotificationEvent.class);
        assertEquals("t", event.titulo());
    }
}
