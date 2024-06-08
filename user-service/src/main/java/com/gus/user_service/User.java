package com.gus.user_service;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity(name = "\"User\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_USER_GENERATOR")
    @SequenceGenerator(name = "SQ_USER_GENERATOR", allocationSize = 1, sequenceName = "SQ_USER")
    private Long id;
    private String username;
    private OffsetDateTime createdAt;
}
