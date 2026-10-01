package com.codexsystem.tracking.dto;

public record EmailCreate(
        Integer codigoPedido,
        String recipientEmail
) {
}
