package com.codexsystem.delivery_serve.controller;

import com.codexsystem.delivery_serve.dto.DeliveryEvent;
import com.codexsystem.delivery_serve.dto.EntregaDto;
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
    @Value("${broker.queue.processamento.name}")
    private String queueName;
    private final RabbitTemplate rabbitTemplate;
    private final DeliveryService service;

    public DeliveryController(DeliveryService deliveryService, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.service = deliveryService;
    }

    @PostMapping("/save")
    public String save(@RequestBody EntregaDto delivery) {
      Entrega newEntrega = service.saveDelivery(delivery);
        DeliveryEvent deliveryEvent = new DeliveryEvent(
                newEntrega.getCodigoPedido(),
                newEntrega.getPedido(),
                newEntrega.getEnderecoDestino(),
                newEntrega.getTransporte()
        );
        System.out.println("Codigo da Entrega: " + newEntrega.getCodigoPedido());
        System.out.println("Codigo do evento: " + deliveryEvent.codigoPedido());
      rabbitTemplate.convertAndSend(queueName, deliveryEvent);
      return "Delivery saved successfully" + newEntrega.getPedido();
    }

    @GetMapping
    public List<Entrega> findAll() {
        return service.getAllDeliveries();
    }

}