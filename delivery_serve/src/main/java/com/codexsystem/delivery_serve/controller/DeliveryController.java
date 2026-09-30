package com.codexsystem.delivery_serve.controller;

import com.codexsystem.delivery_serve.model.Entrega;
import com.codexsystem.delivery_serve.service.DeliveryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    // routing key for the queue where the delivery messages will be sent
    @Value("${broker.queue.processamento.name")
    private String queueName;
    private final RabbitTemplate rabbitTemplate;
    private final DeliveryService service;

    public DeliveryController(DeliveryService deliveryService, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.service = deliveryService;
    }

    @PostMapping("/save")
    public String save(@RequestBody Entrega delivery) {
      Entrega newEntrega = service.saveDelivery(delivery);
      rabbitTemplate.convertAndSend(queueName, newEntrega.getPedido());
      return "Delivery saved successfully" + newEntrega.getId();
    }

    @GetMapping
    public List<Entrega> findAll() {
        return service.getAllDeliveries();
    }

}