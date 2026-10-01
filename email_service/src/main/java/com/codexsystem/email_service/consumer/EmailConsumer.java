package com.codexsystem.email_service.consumer;

import com.codexsystem.email_service.dto.EmailReceive;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @RabbitListener(queues = "${broker.queue.email.name}")
    public void listenerEmailConsumer(EmailReceive emailReceive) {

        System.out.println("Email recebido: " + emailReceive);
    }
}
