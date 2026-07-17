package com.github.nicolasholanda.shardedledger.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table("transactions")
public record Transaction(
        @Id UUID id,
        Long userId,
        BigDecimal amount,
        OffsetDateTime createdAt
) {}
