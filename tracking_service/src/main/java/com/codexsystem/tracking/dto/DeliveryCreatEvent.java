package com.codexsystem.tracking.dto;


public record DeliveryCreatEvent(
        Integer codigoPedido,

        String pedido,

        String email,

        String enderecoDestino,

        String transporte
) {
}
