package com.codexsystem.email_service.dto;

public record DeliveryTrackingEvent(
        String recipientEmail,
        Integer codigoPedido,
        String status
) {}
