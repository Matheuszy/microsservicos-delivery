package com.codexsystem.email_service.dto;

public record DeliveryTrackingEvent(
        Integer codigoPedido,
        String email,
        String status
) {}
