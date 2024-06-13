package com.gus.user_service;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_OUTB_MESS_GENERATOR")
    @SequenceGenerator(name = "SQ_OUTB_MESS_GENERATOR", allocationSize = 1, sequenceName = "SQ_OUTB_MESS")
    private Long id;
    private OffsetDateTime createdAt;
    @Column(columnDefinition = "text")
    private String content;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String destination;
    private Integer tentatives;
    private String headers;
}
