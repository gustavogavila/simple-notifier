package com.gus.user_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OutboxMessageService outboxMessageService;

    @Transactional
    public void registerUser(User user) {
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);
        storeUserEvent(user);
    }

    private void storeUserEvent(User user) {
        try {
            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setContent(createJson(user));
            outboxMessage.setCreatedAt(OffsetDateTime.now());
            outboxMessage.setDestination("user.registration");
            outboxMessage.setStatus(Status.PENDING);
            outboxMessageService.storeEvent(outboxMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String createJson(User user) throws JsonProcessingException {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writeValueAsString(user);
    }

    @Scheduled(fixedDelay = 5000)
    public void pollOutboxAndPublish() {
        List<OutboxMessage> messages = outboxMessageService.getNextMessagesForSending();
//        sendMessages(messages);
    }
}
