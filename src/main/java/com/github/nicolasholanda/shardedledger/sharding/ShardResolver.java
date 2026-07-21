package com.github.nicolasholanda.shardedledger.sharding;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class ShardResolver {

    private static final int SHARD_COUNT = 3;

    public String resolve(Long userId) {
        int shardIndex = Math.floorMod(userId, SHARD_COUNT);
        return "shard" + shardIndex;
    }

    public List<String> allShards() {
        return IntStream.range(0, SHARD_COUNT)
                .mapToObj(index -> "shard" + index)
                .toList();
    }
}
