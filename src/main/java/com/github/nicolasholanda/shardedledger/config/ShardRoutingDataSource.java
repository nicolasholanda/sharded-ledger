package com.github.nicolasholanda.shardedledger.config;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    private static ThreadLocal<String> currentShard = new ThreadLocal<>();

    public static String getCurrentShard() {
        return currentShard.get();
    }

    public static void setCurrentShard(String shardId) {
        currentShard.set(shardId);
    }

    public static void clearCurrentShard() {
        currentShard.remove();
    }

    @Override
    protected @Nullable Object determineCurrentLookupKey() {
        return getCurrentShard();
    }
}
