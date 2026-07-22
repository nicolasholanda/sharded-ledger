package com.github.nicolasholanda.shardedledger.sharding;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

class ReshardingComparisonTest {

    private static final int KEY_COUNT = 100_000;
    private static final int VIRTUAL_NODES = 200;

    @Test
    void modulo_moves_most_keys_when_adding_a_shard() {
        long moved = LongStream.range(0, KEY_COUNT)
                .filter(key -> Math.floorMod(key, 3) != Math.floorMod(key, 4))
                .count();

        double ratio = (double) moved / KEY_COUNT;
        System.out.printf("modulo 3 -> 4: %.1f%% of keys moved%n", ratio * 100);
    }

    @Test
    void ring_moves_few_keys_when_adding_a_shard() {
        ConsistentHashRing before = new ConsistentHashRing(shards(3), VIRTUAL_NODES);
        ConsistentHashRing after = new ConsistentHashRing(shards(4), VIRTUAL_NODES);

        long moved = LongStream.range(0, KEY_COUNT)
                .filter(key -> !before.get(String.valueOf(key)).equals(after.get(String.valueOf(key))))
                .count();

        double ratio = (double) moved / KEY_COUNT;
        System.out.printf("ring 3 -> 4: %.1f%% of keys moved%n", ratio * 100);
    }

    private List<String> shards(int count) {
        return IntStream.range(0, count).mapToObj(i -> "shard" + i).toList();
    }
}
