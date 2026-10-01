package com.codexsystem.delivery_serve.dto;

import com.codexsystem.delivery_serve.enums.Transporte;

public record DeliveryEvent(
        Integer codigoPedido,

        String pedido,

        String enderecoDestino,

        Transporte transporte) {
}
