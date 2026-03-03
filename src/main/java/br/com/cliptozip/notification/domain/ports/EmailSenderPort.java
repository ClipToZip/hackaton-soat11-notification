package br.com.cliptozip.notification.domain.ports;

import br.com.cliptozip.notification.domain.EmailMessage;

public interface EmailSenderPort {
    void send(EmailMessage message);
}
