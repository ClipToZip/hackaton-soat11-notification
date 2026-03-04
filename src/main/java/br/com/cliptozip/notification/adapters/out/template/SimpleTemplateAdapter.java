package br.com.cliptozip.notification.adapters.out.template;

import br.com.cliptozip.notification.domain.EmailMessage;
import br.com.cliptozip.notification.domain.NotificationEvent;
import br.com.cliptozip.notification.domain.ports.TemplatePort;
import org.springframework.stereotype.Component;

@Component
public class SimpleTemplateAdapter implements TemplatePort {

    @Override
    public EmailMessage buildEmail(NotificationEvent event) {
        String status = safe(event.status());

        String subject = "ClipToZip - Status: " + (status.isBlank() ? "Atualizacao" : status);

        String greeting = "Ola, " + safe(event.nomeUsuario()) + "!";
        String title = "Video: " + safe(event.titulo());
        String details = safe(event.mensagem());
        String detailsBlock = details.isBlank() ? "" : "\n\nDetalhes: " + details;

        String body =
                greeting +
                "\n\n" + title +
                "\n\nStatus: " + (status.isBlank() ? "(vazio)" : status) +
                detailsBlock +
                "\n\n- ClipToZip";

        return EmailMessage.builder()
                .to(event.emailUsuario())
                .subject(subject)
                .body(body)
                .build();
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
