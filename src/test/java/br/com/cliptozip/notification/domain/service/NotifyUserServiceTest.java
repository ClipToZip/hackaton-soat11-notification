package br.com.cliptozip.notification.domain.service;

import br.com.cliptozip.notification.domain.EmailMessage;
import br.com.cliptozip.notification.domain.NotificationEvent;
import br.com.cliptozip.notification.domain.ports.DedupPort;
import br.com.cliptozip.notification.domain.ports.EmailSenderPort;
import br.com.cliptozip.notification.domain.ports.TemplatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotifyUserServiceTest {

    private EmailSenderPort emailSender;
    private TemplatePort templatePort;
    private DedupPort dedupPort;
    private LocalValidatorFactoryBean validator;

    private NotifyUserService service;

    @BeforeEach
    void setUp() {
        emailSender = mock(EmailSenderPort.class);
        templatePort = mock(TemplatePort.class);
        dedupPort = mock(DedupPort.class);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        service = new NotifyUserService(emailSender, templatePort, dedupPort, validator);
    }

    @Test
    void shouldSendEmailAndMarkProcessedWhenMessageKeyProvided() {
        var event = new NotificationEvent(
                "Meu vídeo",
                "FINALIZADO",
                "Zip pronto",
                "user@x.com",
                "Samuel"
        );

        when(dedupPort.isDuplicate("m1")).thenReturn(false);
        when(templatePort.buildEmail(event)).thenReturn(EmailMessage.builder()
                .to("user@x.com")
                .subject("s")
                .body("b")
                .build());

        service.notify(event, "m1");

        verify(templatePort).buildEmail(event);
        verify(emailSender).send(Mockito.any(EmailMessage.class));
        verify(dedupPort).markProcessed("m1");
    }

    @Test
    void shouldIgnoreDuplicateMessageKey() {
        var event = new NotificationEvent(
                "Meu vídeo",
                "FINALIZADO",
                "Zip pronto",
                "user@x.com",
                "Samuel"
        );

        when(dedupPort.isDuplicate("dup")).thenReturn(true);

        service.notify(event, "dup");

        verifyNoInteractions(templatePort);
        verifyNoInteractions(emailSender);
        verify(dedupPort, never()).markProcessed(anyString());
    }

    @Test
    void shouldThrowWhenPayloadInvalidAndNotSendAnything() {
        // invalid email + blank title
        var event = new NotificationEvent(
                " ",
                "FINALIZADO",
                "Zip pronto",
                "not-an-email",
                "Samuel"
        );

        assertThrows(IllegalArgumentException.class, () -> service.notify(event, "k"));

        verifyNoInteractions(templatePort);
        verifyNoInteractions(emailSender);
        verifyNoInteractions(dedupPort);
    }
}
