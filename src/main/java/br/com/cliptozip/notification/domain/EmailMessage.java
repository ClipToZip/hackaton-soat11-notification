package br.com.cliptozip.notification.domain;

import lombok.Builder;

@Builder
public record EmailMessage(
        String to,
        String subject,
        String body
) {}
