package com.codexsystem.tracking.dto;

import com.codexsystem.tracking.enums.TrackingStatus;

public record EmailCreate(
        String recipientEmail,
        Integer codigoPedido,
        TrackingStatus status
) {}

