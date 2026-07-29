package com.github.nicolasholanda.shardedledger.controller;

import com.github.nicolasholanda.shardedledger.model.dto.MigrationResult;
import com.github.nicolasholanda.shardedledger.service.ShardMigrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final ShardMigrationService shardMigrationService;

    public AdminController(ShardMigrationService shardMigrationService) {
        this.shardMigrationService = shardMigrationService;
    }

    @PostMapping("/admin/reshard")
    public MigrationResult reshard() {
        return shardMigrationService.migrate();
    }
}
