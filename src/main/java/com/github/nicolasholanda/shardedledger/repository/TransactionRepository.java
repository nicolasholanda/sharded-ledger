package com.github.nicolasholanda.shardedledger.repository;

import com.github.nicolasholanda.shardedledger.model.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {

    List<Transaction> findByUserId(Long userId);
}
