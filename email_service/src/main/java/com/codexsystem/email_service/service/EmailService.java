package com.codexsystem.email_service.service;

import com.codexsystem.email_service.dto.DeliveryTrackingEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDeliveryNotification(
            DeliveryTrackingEvent emailReceive
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(emailReceive.recipientEmail());
        message.setSubject("Seu pedido está em trânsito");

        message.setText("""
                Olá!

                Seu pedido %d está em trânsito.

                Em breve ele chegará ao endereço informado.

                Atenciosamente,
                Codex System
                """.formatted(emailReceive.codigoPedido()));

        mailSender.send(message);
    }
}
