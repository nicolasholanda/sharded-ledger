package com.github.nicolasholanda.shardedledger.controller;

import com.github.nicolasholanda.shardedledger.model.dto.MigrationResult;
import com.github.nicolasholanda.shardedledger.service.ShardMigrationService;
import com.github.nicolasholanda.shardedledger.sharding.ShardMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminController {

    private final ShardMigrationService shardMigrationService;
    private final ShardMap shardMap;

    public AdminController(ShardMigrationService shardMigrationService, ShardMap shardMap) {
        this.shardMigrationService = shardMigrationService;
        this.shardMap = shardMap;
    }

    @GetMapping("/admin/shard-map")
    public Map<String, Long> shardMap() {
        return shardMap.distribution();
    }

    @PostMapping("/admin/shard-map/reassign")
    public Map<String, Long> reassign(@RequestParam String fromShard,
                                      @RequestParam String toShard,
                                      @RequestParam int buckets) {
        shardMap.reassign(fromShard, toShard, buckets);
        return shardMap.distribution();
    }

    @PostMapping("/admin/reshard")
    public MigrationResult reshard() {
        return shardMigrationService.migrate();
    }
}
