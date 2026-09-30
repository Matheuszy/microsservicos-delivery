package com.codexsystem.tracking.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TrackingConsumer {

    @RabbitListener(queues = "${broker.queue.processamento.name}")
    public void listererTrackingConsumer(@Payload String message) {
        System.out.println("Mensagem recebida: " + message);
    }

}
