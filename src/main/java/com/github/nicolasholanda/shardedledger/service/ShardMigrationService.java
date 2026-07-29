package com.github.nicolasholanda.shardedledger.service;

import com.github.nicolasholanda.shardedledger.config.ShardRoutingDataSource;
import com.github.nicolasholanda.shardedledger.model.Transaction;
import com.github.nicolasholanda.shardedledger.model.dto.MigrationResult;
import com.github.nicolasholanda.shardedledger.repository.TransactionRepository;
import com.github.nicolasholanda.shardedledger.sharding.ShardResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShardMigrationService {

    private static final Logger log = LoggerFactory.getLogger(ShardMigrationService.class);

    private final TransactionRepository transactionRepository;
    private final JdbcAggregateTemplate aggregateTemplate;
    private final ShardResolver shardResolver;

    public ShardMigrationService(TransactionRepository transactionRepository,
                                 JdbcAggregateTemplate aggregateTemplate,
                                 ShardResolver shardResolver) {
        this.transactionRepository = transactionRepository;
        this.aggregateTemplate = aggregateTemplate;
        this.shardResolver = shardResolver;
    }

    public MigrationResult migrate() {
        long scanned = 0;
        long moved = 0;

        for (String source : shardResolver.allShards()) {
            for (Transaction transaction : readAll(source)) {
                scanned++;
                String target = shardResolver.resolve(transaction.userId());
                if (target.equals(source)) {
                    continue;
                }
                move(transaction, source, target);
                moved++;
            }
        }

        log.info("Resharding finished: scanned {}, moved {}", scanned, moved);
        return new MigrationResult(scanned, moved);
    }

    private List<Transaction> readAll(String shard) {
        ShardRoutingDataSource.setCurrentShard(shard);
        try {
            List<Transaction> rows = new ArrayList<>();
            transactionRepository.findAll().forEach(rows::add);
            return rows;
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }
    }

    private void move(Transaction transaction, String source, String target) {
        ShardRoutingDataSource.setCurrentShard(target);
        try {
            aggregateTemplate.insert(transaction);
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }

        ShardRoutingDataSource.setCurrentShard(source);
        try {
            transactionRepository.deleteById(transaction.id());
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }

        log.info("Moved transaction {} (user {}) from {} to {}",
                transaction.id(), transaction.userId(), source, target);
    }
}
