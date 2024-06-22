package com.gus.user_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OutboxMessageService outboxMessageService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void registerUser(User user) {
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);
        storeUserEvent(user);
    }

    private void storeUserEvent(User user) {
        try {
            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setContent(entityToJson(user));
            outboxMessage.setCreatedAt(OffsetDateTime.now());
            outboxMessage.setDestination("user.registration");
            outboxMessage.setStatus(Status.PENDING);
            outboxMessage.setTentatives(0);
            outboxMessageService.storeEvent(outboxMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String entityToJson(User user) throws JsonProcessingException {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writeValueAsString(user);
    }

    @Scheduled(fixedDelay = 60000)
    public void pollOutboxAndPublish() {
        List<OutboxMessage> outboxMessages = outboxMessageService.getNextMessagesForSending();

        for (OutboxMessage outboxMessage : outboxMessages) {
            try {
                User user = jsonToEntity(outboxMessage.getContent());
                UserCreatedEvent userCreatedEvent = new UserCreatedEvent(user.getId(), user.getUsername(), user.getCreatedAt());
                sendMessage(userCreatedEvent);
                outboxMessage.setStatus(Status.SENT);
                outboxMessageService.save(outboxMessage);
            } catch (Exception e) {
                log.error("Falha ao tentar enviar para o broker", e);
                if (outboxMessage.getTentatives() <= 20) {
                    outboxMessage.setTentatives(outboxMessage.getTentatives() + 1);
                    continue;
                }
                outboxMessage.setStatus(Status.ERROR);
            }
        }
    }

    private User jsonToEntity(String content) throws JsonProcessingException {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(content, User.class);
    }

    private void sendMessage(UserCreatedEvent userCreatedEvent) {
        var exchange = "users.v1.user-created";
        rabbitTemplate.convertAndSend(exchange, "", userCreatedEvent);
    }
}
