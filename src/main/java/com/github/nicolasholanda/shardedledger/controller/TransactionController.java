package com.github.nicolasholanda.shardedledger.controller;

import com.github.nicolasholanda.shardedledger.model.Transaction;
import com.github.nicolasholanda.shardedledger.model.dto.CreateTransactionRequest;
import com.github.nicolasholanda.shardedledger.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request.userId(), request.amount());
    }

    @GetMapping("/users/{id}/transactions")
    public List<Transaction> getByUser(@PathVariable("id") Long id) {
        return transactionService.getTransactionsForUser(id);
    }
}
