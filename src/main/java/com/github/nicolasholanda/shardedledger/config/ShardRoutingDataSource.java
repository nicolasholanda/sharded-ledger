package com.github.nicolasholanda.shardedledger.config;

import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    private static final String SHARD_MDC_KEY = "shard";

    private static ThreadLocal<String> currentShard = new ThreadLocal<>();

    public static String getCurrentShard() {
        return currentShard.get();
    }

    public static void setCurrentShard(String shardId) {
        currentShard.set(shardId);
        MDC.put(SHARD_MDC_KEY, shardId);
    }

    public static void clearCurrentShard() {
        currentShard.remove();
        MDC.remove(SHARD_MDC_KEY);
    }

    @Override
    protected @Nullable Object determineCurrentLookupKey() {
        return getCurrentShard();
    }
}
