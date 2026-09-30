package com.codexsystem.delivery_serve.service;

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
    public Entrega saveDelivery(Entrega delivery) {
      if (delivery == null) {
        throw new IllegalArgumentException("Delivery cannot be null");
      }

      return deliveryRepository.save(delivery);
    }

    public List<Entrega> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

}
