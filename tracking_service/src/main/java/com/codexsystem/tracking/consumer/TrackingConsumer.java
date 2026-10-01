package com.codexsystem.tracking.consumer;

import com.codexsystem.tracking.dto.DeliveryCreatEvent;
import com.codexsystem.tracking.dto.EmailCreate;
import com.codexsystem.tracking.enums.TrackingStatus;
import com.codexsystem.tracking.model.Tracking;
import com.codexsystem.tracking.producer.TrackingProducer;
import com.codexsystem.tracking.repository.TrackingRespository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TrackingConsumer {
    private final TrackingProducer producer;
    private final TrackingRespository trackingRepository;

    public TrackingConsumer(TrackingProducer trackingProducer, TrackingRespository trackingRepository) {
        this.producer = trackingProducer;
        this.trackingRepository = trackingRepository;
    }

    @RabbitListener(queues = "${broker.queue.processamento.name}")
    public void listererTrackingConsumer(DeliveryCreatEvent event) {
        System.out.println(event);
        Tracking tracking = new Tracking();

        tracking.setCodigoPedido(event.codigoPedido());
        tracking.setEmail(event.email());
        tracking.setStatus(TrackingStatus.IN_TRANSIT);

        trackingRepository.save(tracking);

        producer.produceTrackingData(new EmailCreate(event.email(), event.codigoPedido(), TrackingStatus.IN_TRANSIT));
    }
}




