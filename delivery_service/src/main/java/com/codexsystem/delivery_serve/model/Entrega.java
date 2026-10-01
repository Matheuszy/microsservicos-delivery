package com.codexsystem.delivery_serve.model;

import com.codexsystem.delivery_serve.enums.Transporte;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery")
public class Entrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pedido;

    private String enderecoDestino;

    private BigDecimal valor;

    private String tipoProduto;

    private String email;

    private Integer codigoPedido;

    @Enumerated(EnumType.STRING)
    private Transporte transporte;

    public Entrega() {
    }

    public Entrega(Integer codigoPedido, String pedido, String email, BigDecimal valor, String tipoProduto, String enderecoDestino, Transporte transporte) {
        this.codigoPedido = codigoPedido;
        this.pedido = pedido;
        this.email = email;
        this.valor = valor;
        this.tipoProduto = tipoProduto;
        this.enderecoDestino = enderecoDestino;
        this.transporte = transporte;
    }

    public Integer getCodigoPedido() {
        return codigoPedido;
    }

    public void setCodigoPedido(Integer codigoPedido) {
        this.codigoPedido = codigoPedido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Transporte getTransporte() {
        return transporte;
    }

    public void setTransporte(Transporte transporte) {
        this.transporte = transporte;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public void setTipoProduto(String tipoProduto) {
        this.tipoProduto = tipoProduto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getEnderecoDestino() {
        return enderecoDestino;
    }

    public void setEnderecoDestino(String enderecoDestino) {
        this.enderecoDestino = enderecoDestino;
    }

    public String getPedido() {
        return pedido;
    }

    public void setPedido(String pedido) {
        this.pedido = pedido;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
