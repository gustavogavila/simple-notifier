package com.gus.notification_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DLQListener {

    private static final String DLQ_NAME = "users.v1.user-created.dlx.email-notification.dlq";
    private static final String DLQ_PARKING_LOT_NAME = "users.v1.user-created.dlx.email-notification.dlq.parking-lot";
    private static final String X_RETRY_HEADER = "X_RETRY_HEADER";
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = DLQ_NAME)
    public void process(UserCreatedEvent event, @Headers Map<String, Object> headers) {

        Integer retryHeader = (Integer) headers.get(X_RETRY_HEADER);

        if (retryHeader == null) {
            retryHeader = 0;
        }
        // TODO implementar intervalo inicial e multiplicador de intervalo
        if (retryHeader < 3) {
            log.info(String.format("Tentando reprocessar pela %sa vez o evento de id %s e nome de usuario %s",
                    retryHeader,
                    event.getId(),
                    event.getUsername()));
            final Map<String, Object> updatedHeaders = new HashMap<>(headers);

            updatedHeaders.put(X_RETRY_HEADER, ++retryHeader);

            final MessagePostProcessor messagePostProcessor = message -> {
                MessageProperties messageProperties = message.getMessageProperties();
                updatedHeaders.forEach(messageProperties::setHeader);
                return message;
            };
            rabbitTemplate.convertAndSend(DLQ_NAME, event, messagePostProcessor);
        } else {
            log.info(String.format("Enviando evento de id %s e usuario %s para o Parking Lot", event.getId(), event.getUsername()));
            rabbitTemplate.convertAndSend(DLQ_PARKING_LOT_NAME, event);
        }
    }
}
