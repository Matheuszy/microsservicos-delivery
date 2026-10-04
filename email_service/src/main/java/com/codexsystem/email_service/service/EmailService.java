package com.codexsystem.email_service.service;

import com.codexsystem.email_service.dto.DeliveryTrackingEvent;
import com.codexsystem.email_service.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendDeliveryNotification(
            DeliveryTrackingEvent event
    ) {

        try {

            Context context = new Context();

            context.setVariable("codigoPedido", event.codigoPedido());
            context.setVariable("status", event.status());

            String htmlContent =
                    templateEngine.process(
                            "delivery-in-transit",
                            context
                    );

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setTo(event.recipientEmail());
            helper.setSubject("Seu pedido está em trânsito");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new EmailSendingException(
                    "Erro ao enviar e-mail para "
                            + event.recipientEmail(),
                    e
            );
        }
    }
}