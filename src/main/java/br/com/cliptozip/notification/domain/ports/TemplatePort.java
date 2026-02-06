package br.com.cliptozip.notification.domain.ports;

import br.com.cliptozip.notification.domain.EmailMessage;
import br.com.cliptozip.notification.domain.NotificationEvent;

public interface TemplatePort {
    EmailMessage buildEmail(NotificationEvent event);
}
