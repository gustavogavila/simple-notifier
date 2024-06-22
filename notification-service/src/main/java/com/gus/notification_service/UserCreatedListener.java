package com.gus.notification_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserCreatedListener {

    private final EmailService emailService;
    private final AppProperties properties;

    @RabbitListener(queues = "users.v1.user-created.email-notification")
    public void onUserCreated(UserCreatedEvent event) {
        log.info("ID RECEBIDO: " + event.toString());

        if (event.getUsername().contains("pereira")) {
            log.error("Falha no consumo da mensagem");
            throw new RuntimeException("Falha no consumo da mensagem");
        }

        var body = String.format("O usuário %s foi registrado com o id %s em %s",
                event.getUsername(),
                event.getId(),
                event.getCreatedAt().toString());
        emailService.sendEmail(properties.getNotificationEmail(), "Registro de novo usuário", body);
    }
}
