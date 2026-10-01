package com.codexsystem.email_service.consumer;

import com.codexsystem.email_service.dto.DeliveryTrackingEvent;
import com.codexsystem.email_service.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${broker.queue.email.name}")
    public void listenerEmailConsumer(DeliveryTrackingEvent trackingEvent) {

        System.out.println("Email recebido: " + trackingEvent.recipientEmail());

        emailService.sendDeliveryNotification(trackingEvent);
    }
}
