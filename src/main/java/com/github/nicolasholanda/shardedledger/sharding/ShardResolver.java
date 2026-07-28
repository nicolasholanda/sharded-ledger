package com.github.nicolasholanda.shardedledger.sharding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShardResolver {

    private static final int VIRTUAL_NODES_PER_SHARD = 200;

    private final List<String> shards;
    private final ConsistentHashRing ring;

    public ShardResolver(@Value("${app.shards}") List<String> shards) {
        this.shards = shards;
        this.ring = new ConsistentHashRing(shards, VIRTUAL_NODES_PER_SHARD);
    }

    public String resolve(Long userId) {
        return ring.get(String.valueOf(userId));
    }

    public List<String> allShards() {
        return shards;
    }
}
