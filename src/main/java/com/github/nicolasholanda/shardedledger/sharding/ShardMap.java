package com.github.nicolasholanda.shardedledger.sharding;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Component
@DependsOn("flywayMigrator")
public class ShardMap {

    private static final Logger log = LoggerFactory.getLogger(ShardMap.class);

    private static final int BUCKET_COUNT = 1024;
    private static final int VIRTUAL_NODES_PER_SHARD = 200;

    private final JdbcTemplate metadataJdbc;
    private final List<String> configuredShards;
    private final Map<Integer, String> cache = new ConcurrentHashMap<>();

    public ShardMap(@Qualifier("shard0DataSource") DataSource metadataDataSource,
                    @Value("${app.shards}") List<String> configuredShards) {
        this.metadataJdbc = new JdbcTemplate(metadataDataSource);
        this.configuredShards = configuredShards;
    }

    @PostConstruct
    public void init() {
        List<Map<String, Object>> rows = metadataJdbc.queryForList("SELECT bucket, shard FROM shard_map");
        if (rows.isEmpty()) {
            seed();
        } else {
            rows.forEach(row -> cache.put((Integer) row.get("bucket"), (String) row.get("shard")));
            log.info("Loaded shard map with {} buckets", cache.size());
        }
    }

    private void seed() {
        ConsistentHashRing ring = new ConsistentHashRing(configuredShards, VIRTUAL_NODES_PER_SHARD);
        List<Object[]> batch = IntStream.range(0, BUCKET_COUNT)
                .mapToObj(bucket -> {
                    String shard = ring.get(String.valueOf(bucket));
                    cache.put(bucket, shard);
                    return new Object[]{bucket, shard};
                })
                .toList();
        metadataJdbc.batchUpdate("INSERT INTO shard_map (bucket, shard) VALUES (?, ?)", batch);
        log.info("Seeded shard map with {} buckets across {}", BUCKET_COUNT, configuredShards);
    }

    public String shardFor(Long userId) {
        return cache.get(bucketFor(userId));
    }

    public List<String> allShards() {
        return configuredShards;
    }

    public Map<String, Long> distribution() {
        Map<String, Long> result = new TreeMap<>();
        cache.values().forEach(shard -> result.merge(shard, 1L, Long::sum));
        return result;
    }

    public int reassign(String fromShard, String toShard, int count) {
        List<Integer> buckets = cache.entrySet().stream()
                .filter(entry -> entry.getValue().equals(fromShard))
                .map(Map.Entry::getKey)
                .limit(count)
                .toList();

        for (Integer bucket : buckets) {
            metadataJdbc.update("UPDATE shard_map SET shard = ? WHERE bucket = ?", toShard, bucket);
            cache.put(bucket, toShard);
        }

        log.info("Reassigned {} buckets from {} to {}", buckets.size(), fromShard, toShard);
        return buckets.size();
    }

    private int bucketFor(Long userId) {
        return Math.floorMod(userId, BUCKET_COUNT);
    }
}
