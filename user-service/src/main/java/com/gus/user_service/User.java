package com.gus.user_service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "\"User\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_USER_GENERATOR")
    @SequenceGenerator(name = "SQ_USER_GENERATOR", allocationSize = 1, sequenceName = "SQ_USER")
    private Long id;
    private String username;
    private OffsetDateTime createdAt;
}
