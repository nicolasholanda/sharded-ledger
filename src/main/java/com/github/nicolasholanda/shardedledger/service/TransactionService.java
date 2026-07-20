package com.github.nicolasholanda.shardedledger.service;

import com.github.nicolasholanda.shardedledger.config.ShardRoutingDataSource;
import com.github.nicolasholanda.shardedledger.model.Transaction;
import com.github.nicolasholanda.shardedledger.repository.TransactionRepository;
import com.github.nicolasholanda.shardedledger.sharding.ShardResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TransactionService {

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
            return transactionRepository.save(transaction);
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForUser(Long userId) {
        ShardRoutingDataSource.setCurrentShard(shardResolver.resolve(userId));
        try {
            return transactionRepository.findByUserId(userId);
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }
    }
}
