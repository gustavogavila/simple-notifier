package com.gus.user_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxMessageService {
    private final OutboxMessageRepository outboxMessageRepository;

    @Transactional
    public void storeEvent(OutboxMessage outboxMessage) {
        outboxMessageRepository.save(outboxMessage);
    }

    public List<OutboxMessage> getNextMessagesForSending() {
        List<OutboxMessage> allPending = outboxMessageRepository.findAllByStatus(Status.PENDING);
        System.out.println("Pendentes: " + allPending.size() + " | " + allPending);
        List<OutboxMessage> allSent = allPending.stream().peek(message -> message.setStatus(Status.SENT)).toList();
        System.out.println("Enviados: " + allSent.size() + " | " + allSent);
        return allSent;
    }
}
