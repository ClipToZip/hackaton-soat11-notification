package br.com.cliptozip.notification.domain.service;

import br.com.cliptozip.notification.domain.NotificationEvent;
import br.com.cliptozip.notification.domain.ports.DedupPort;
import br.com.cliptozip.notification.domain.ports.EmailSenderPort;
import br.com.cliptozip.notification.domain.ports.TemplatePort;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotifyUserService {

    private static final Logger log = LoggerFactory.getLogger(NotifyUserService.class);

    private final EmailSenderPort emailSender;
    private final TemplatePort templatePort;
    private final DedupPort dedupPort;
    private final Validator validator;

    public void notify(NotificationEvent event, String messageKey) {
        validate(event);

        if (messageKey != null && !messageKey.isBlank()) {
            if (dedupPort.isDuplicate(messageKey)) {
                log.info("Duplicate message ignored. key={}", messageKey);
                return;
            }
        }

        var email = templatePort.buildEmail(event);
        emailSender.send(email);

        if (messageKey != null && !messageKey.isBlank()) {
            dedupPort.markProcessed(messageKey);
        }
    }

    private void validate(NotificationEvent event) {
        Set<ConstraintViolation<NotificationEvent>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Invalid event payload: ");
            for (var v : violations) {
                sb.append(v.getPropertyPath()).append(" ").append(v.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }
}
