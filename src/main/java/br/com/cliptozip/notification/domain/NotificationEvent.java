package br.com.cliptozip.notification.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Domain event representing a notification request coming from SQS.
 */
public record NotificationEvent(
        @NotBlank String titulo,
        @NotBlank String status,
        @NotBlank String mensagem,
        @NotBlank @Email String emailUsuario,
        @NotBlank String nomeUsuario
) {
}
