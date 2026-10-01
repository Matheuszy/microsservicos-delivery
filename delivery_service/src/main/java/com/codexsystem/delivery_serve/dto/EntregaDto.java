package com.codexsystem.delivery_serve.dto;

import com.codexsystem.delivery_serve.enums.Transporte;

import java.math.BigDecimal;

public record EntregaDto(
        Integer codigoPedido,

        String pedido,

        BigDecimal valor,

        String tipoProduto,

        String enderecoDestino,

        Transporte transporte
) {
}
