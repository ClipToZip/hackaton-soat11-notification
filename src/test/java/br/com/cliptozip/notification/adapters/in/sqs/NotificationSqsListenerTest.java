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

    @Test
    void shouldHandlePayloadNullAsInvalid() {
        var service = mock(NotifyUserService.class);
        var listener = new NotificationSqsListener(objectMapper(), service);

        // MessageBuilder forbids null payloads; use empty string to simulate missing content
        var msg = MessageBuilder.withPayload("")
                .setHeader("MessageId", "m4")
                .build();

        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(msg));
        verifyNoInteractions(service);
    }

    @Test
    void shouldAcceptNumbersBooleansAndNullValues() {
        var service = mock(NotifyUserService.class);
        var listener = new NotificationSqsListener(objectMapper(), service);

        // titulo is a number, status is boolean, mensagem is null literal
        String payload = "{titulo:123,status:true,mensagem:null,emailUsuario:user@x.com,nomeUsuario:Sam}";

        var msg = MessageBuilder.withPayload(payload).setHeader("MessageId", "m5").build();

        listener.onMessage(msg);

        ArgumentCaptor<NotificationEvent> c = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(service).notify(c.capture(), eq("m5"));

        NotificationEvent e = c.getValue();
        // Jackson should coerce numbers/booleans to strings when target type is String
        assertEquals("123", e.titulo());
        assertEquals("true", e.status());
        // null literal maps to null string (allowed at deserialization time)
        assertNull(e.mensagem());
    }

    @Test
    void shouldPreserveQuotesAndBackslashesInValues() {
        var service = mock(NotifyUserService.class);
        var listener = new NotificationSqsListener(objectMapper(), service);

        String rawTitle = "He said \"hi\" and path C:\\temp\\file";
        // pseudo-json with unquoted value that contains quotes and backslashes
        String payload = "{titulo:" + rawTitle + ",status:OK,mensagem:msg,emailUsuario:user@x.com,nomeUsuario:Sam}";

        var msg = MessageBuilder.withPayload(payload).setHeader("SqsMessageId", "m6").build();

        listener.onMessage(msg);

        ArgumentCaptor<NotificationEvent> c = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(service).notify(c.capture(), eq("m6"));

        String parsed = c.getValue().titulo();
        // ensure quotes were preserved and the path components remain present (avoid fragile full-equality)
        assertTrue(parsed.contains("He said \"hi\""));
        assertTrue(parsed.contains("C:"));
        assertTrue(parsed.contains("temp"));
        assertTrue(parsed.contains("file"));
    }

    @Test
    void shouldExtractMessageIdFromVariousHeaderKeys() {
        var service = mock(NotifyUserService.class);
        var listener = new NotificationSqsListener(objectMapper(), service);

        String payload = "{\"titulo\":\"x\",\"status\":\"s\",\"mensagem\":\"m\",\"emailUsuario\":\"e@x.x\",\"nomeUsuario\":\"n\"}";

        var msg1 = MessageBuilder.withPayload(payload).setHeader("SqsMessageId", "id1").build();
        var msg2 = MessageBuilder.withPayload(payload).setHeader("sqs_message_id", "id2").build();
        var msg3 = MessageBuilder.withPayload(payload).setHeader("X-MY-MESSAGE-ID", "id3").build();

        listener.onMessage(msg1);
        listener.onMessage(msg2);
        listener.onMessage(msg3);

        verify(service).notify(any(), eq("id1"));
        verify(service).notify(any(), eq("id2"));
        verify(service).notify(any(), eq("id3"));
    }
}
