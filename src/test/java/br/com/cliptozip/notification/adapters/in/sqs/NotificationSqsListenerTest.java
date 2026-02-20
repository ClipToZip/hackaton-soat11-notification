package br.com.cliptozip.notification.adapters.in.sqs;

import br.com.cliptozip.notification.domain.NotificationEvent;
import br.com.cliptozip.notification.domain.service.NotifyUserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationSqsListenerTest {

    private ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    @Test
    void shouldParseStrictJsonAndCallDomainService() {
        var service = mock(NotifyUserService.class);
        var listener = new NotificationSqsListener(objectMapper(), service);

        String payload = "{" +
                "\"titulo\":\"Meu vídeo\"," +
                "\"status\":\"FINALIZADO\"," +
                "\"mensagem\":\"Zip pronto\"," +
                "\"emailUsuario\":\"user@x.com\"," +
                "\"nomeUsuario\":\"Samuel\"" +
                "}";

        var msg = MessageBuilder.withPayload(payload)
                .setHeader("MessageId", "m1")
                .build();

        listener.onMessage(msg);

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(service).notify(eventCaptor.capture(), eq("m1"));

        NotificationEvent e = eventCaptor.getValue();
        assertEquals("Meu vídeo", e.titulo());
        assertEquals("FINALIZADO", e.status());
        assertEquals("Zip pronto", e.mensagem());
        assertEquals("user@x.com", e.emailUsuario());
        assertEquals("Samuel", e.nomeUsuario());
    }

    @Test
    void shouldRepairPseudoJsonAndCallDomainService() {
        var service = mock(NotifyUserService.class);
        var listener = new NotificationSqsListener(objectMapper(), service);

        String payload = "{titulo:Meu vídeo,status:FINALIZADO,mensagem:Zip pronto,emailUsuario:user@x.com,nomeUsuario:Samuel}";

        var msg = MessageBuilder.withPayload(payload)
                .setHeader("messageId", "m2")
                .build();

        listener.onMessage(msg);

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(service).notify(eventCaptor.capture(), eq("m2"));

        assertEquals("Meu vídeo", eventCaptor.getValue().titulo());
    }

    @Test
    void shouldThrowWhenPayloadUnparseable() {
        var service = mock(NotifyUserService.class);
        var listener = new NotificationSqsListener(objectMapper(), service);

        var msg = MessageBuilder.withPayload("not-json")
                .setHeader("MessageId", "m3")
                .build();

        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(msg));

        verifyNoInteractions(service);
    }
}
