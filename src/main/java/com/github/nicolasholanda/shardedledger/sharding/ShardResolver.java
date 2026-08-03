package com.github.nicolasholanda.shardedledger.sharding;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShardResolver {

    private final ShardMap shardMap;

    public ShardResolver(ShardMap shardMap) {
        this.shardMap = shardMap;
    }

    public String resolve(Long userId) {
        return shardMap.shardFor(userId);
    }

    public List<String> allShards() {
        return shardMap.allShards();
    }
}
