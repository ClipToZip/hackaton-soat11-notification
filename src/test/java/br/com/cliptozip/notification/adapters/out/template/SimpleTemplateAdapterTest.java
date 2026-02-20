package br.com.cliptozip.notification.adapters.out.template;

import br.com.cliptozip.notification.domain.NotificationEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTemplateAdapterTest {

    private final SimpleTemplateAdapter adapter = new SimpleTemplateAdapter();

    @Test
    void shouldCreateFinalizadoEmail() {
        var event = new NotificationEvent(
                "Meu vídeo",
                "FINALIZADO",
                "Zip pronto",
                "user@x.com",
                "Samuel"
        );

        var email = adapter.buildEmail(event);

        assertEquals("user@x.com", email.to());
        assertTrue(email.subject().contains("Status"));
        assertTrue(email.body().contains("FINALIZADO"));
        assertTrue(email.body().contains("Meu vídeo"));
    }

    @Test
    void shouldHandleBlankStatusAndBlankDetails() {
        var event = new NotificationEvent(
                "Meu vídeo",
                "   ",
                "   ",
                "user@x.com",
                "Samuel"
        );

        var email = adapter.buildEmail(event);

        assertEquals("user@x.com", email.to());
        assertTrue(email.subject().contains("Atualizacao"));
        assertTrue(email.body().contains("Status: (vazio)"));
        assertFalse(email.body().contains("Detalhes:"));
    }
}
