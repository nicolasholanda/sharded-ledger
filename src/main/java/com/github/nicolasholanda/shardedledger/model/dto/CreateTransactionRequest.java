package com.github.nicolasholanda.shardedledger.model.dto;

import java.math.BigDecimal;

public record CreateTransactionRequest(Long userId, BigDecimal amount) {
}
