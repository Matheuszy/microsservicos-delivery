package com.codexsystem.email_service.service;

import com.codexsystem.email_service.dto.DeliveryTrackingEvent;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendDeliveryNotification(
            DeliveryTrackingEvent emailReceive
    ) {

        String subject = "Seu pedido está em trânsito";

        String body = """
                Olá!

                Seu pedido %d está em trânsito.

                Em breve ele chegará ao endereço informado.

                Atenciosamente,
                Codex System
                """.formatted(
                emailReceive.codigoPedido()
        );

        System.out.println("Para: " + emailReceive.recipientEmail());
        System.out.println("Assunto: " + subject);
        System.out.println("Body: " + body);
    }
}
