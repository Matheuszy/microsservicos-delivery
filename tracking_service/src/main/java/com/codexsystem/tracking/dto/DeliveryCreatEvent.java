package com.codexsystem.tracking.dto;


public record DeliveryCreatEvent(
        Integer codigoPedido,

        String pedido,

        String enderecoDestino,

        String transporte
) {
}
