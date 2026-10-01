package com.codexsystem.email_service.dto;

public record EmailCreatedEvent(
        String emailOrigem,

        String emailDestino,

        String assunto
) {
}
