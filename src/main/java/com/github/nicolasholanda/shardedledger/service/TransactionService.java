package com.github.nicolasholanda.shardedledger.service;

import com.github.nicolasholanda.shardedledger.config.ShardRoutingDataSource;
import com.github.nicolasholanda.shardedledger.model.Transaction;
import com.github.nicolasholanda.shardedledger.repository.TransactionRepository;
import com.github.nicolasholanda.shardedledger.sharding.ShardResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final ShardResolver shardResolver;

    public TransactionService(TransactionRepository transactionRepository, ShardResolver shardResolver) {
        this.transactionRepository = transactionRepository;
        this.shardResolver = shardResolver;
    }

    @Transactional
    public Transaction createTransaction(Long userId, BigDecimal amount) {
        ShardRoutingDataSource.setCurrentShard(shardResolver.resolve(userId));
        try {
            Transaction transaction = new Transaction(null, userId, amount, OffsetDateTime.now());
            Transaction saved = transactionRepository.save(transaction);
            log.info("Created transaction {} for user {} with amount {}", saved.id(), userId, amount);
            return saved;
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForUser(Long userId) {
        ShardRoutingDataSource.setCurrentShard(shardResolver.resolve(userId));
        try {
            List<Transaction> transactions = transactionRepository.findByUserId(userId);
            log.info("Found {} transactions for user {}", transactions.size(), userId);
            return transactions;
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }
    }
}
