package com.gus.user_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreatedEvent {
    private Long id;
    private String username;
    private OffsetDateTime createdAt;
}
