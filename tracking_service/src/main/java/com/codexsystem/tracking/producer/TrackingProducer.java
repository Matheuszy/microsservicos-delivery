package com.codexsystem.tracking.producer;

import com.codexsystem.tracking.dto.EmailCreate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrackingProducer {

    private final RabbitTemplate rabbitTemplate;

    public TrackingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceTrackingData(String receiptEmail) {

        EmailCreate emailCreate = new EmailCreate(receiptEmail);

        rabbitTemplate.convertAndSend(
                "email.queue",
                emailCreate
        );
    }
}