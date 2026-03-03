package br.com.cliptozip.notification.adapters.out.email;

import br.com.cliptozip.notification.domain.EmailMessage;
import br.com.cliptozip.notification.domain.ports.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    @Override
    public void send(EmailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.body());

        mailSender.send(mail);
        log.info("Email sent. to={} subject={}", message.to(), message.subject());
    }
}
