package com.codexsystem.delivery_serve.service;

import com.codexsystem.delivery_serve.dto.DeliveryEvent;
import com.codexsystem.delivery_serve.dto.EntregaDto;
import com.codexsystem.delivery_serve.model.Entrega;
import com.codexsystem.delivery_serve.repository.EntregaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryService {

    private final EntregaRepository deliveryRepository;
    public DeliveryService(EntregaRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public Entrega saveDelivery(EntregaDto delivery) {
      if (delivery == null) {
        throw new IllegalArgumentException("Delivery cannot be null");
      }
        Entrega newDelivery = new Entrega(
                delivery.codigoPedido(),
                delivery.pedido(),
                delivery.valor(),
                delivery.tipoProduto(),
                delivery.enderecoDestino(),
                delivery.transporte()
        );

       deliveryRepository.save(newDelivery);

       return  newDelivery;
    }

    public List<Entrega> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

}
