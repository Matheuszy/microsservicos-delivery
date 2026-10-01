package com.codexsystem.tracking.producer;

import com.codexsystem.tracking.dto.EmailCreate;
import com.codexsystem.tracking.enums.TrackingStatus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrackingProducer {

    private final RabbitTemplate rabbitTemplate;

    public TrackingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceTrackingData(EmailCreate emailCreate) {

        EmailCreate newEmailCreate = new EmailCreate(
                emailCreate.recipientEmail(),
                emailCreate.codigoPedido(),
                TrackingStatus.IN_TRANSIT
        );

        rabbitTemplate.convertAndSend(
                "email.queue",
                newEmailCreate
        );
    }
}