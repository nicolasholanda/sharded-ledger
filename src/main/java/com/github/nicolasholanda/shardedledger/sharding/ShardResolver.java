package com.github.nicolasholanda.shardedledger.sharding;

import org.springframework.stereotype.Component;

@Component
public class ShardResolver {

    private static final int SHARD_COUNT = 3;

    public String resolve(Long userId) {
        int shardIndex = Math.floorMod(userId, SHARD_COUNT);
        return "shard" + shardIndex;
    }
}
