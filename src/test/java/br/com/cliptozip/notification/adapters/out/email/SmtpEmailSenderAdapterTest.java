package br.com.cliptozip.notification.adapters.out.email;

import br.com.cliptozip.notification.domain.EmailMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmtpEmailSenderAdapterTest {

    @Test
    void shouldBuildAndSendSimpleMailMessage() {
        JavaMailSender sender = mock(JavaMailSender.class);
        var adapter = new SmtpEmailSenderAdapter(sender);
        ReflectionTestUtils.setField(adapter, "from", "noreply@cliptozip.com");

        var msg = EmailMessage.builder()
                .to("user@x.com")
                .subject("Assunto")
                .body("Corpo")
                .build();

        adapter.send(msg);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("noreply@cliptozip.com", sent.getFrom());
        assertArrayEquals(new String[]{"user@x.com"}, sent.getTo());
        assertEquals("Assunto", sent.getSubject());
        assertEquals("Corpo", sent.getText());
    }
}
