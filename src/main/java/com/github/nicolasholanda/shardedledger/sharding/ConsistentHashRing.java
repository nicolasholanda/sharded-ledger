package com.github.nicolasholanda.shardedledger.sharding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashRing {

    private final int virtualNodesPerShard;
    private final TreeMap<Long, String> ring = new TreeMap<>();

    public ConsistentHashRing(Collection<String> shards, int virtualNodesPerShard) {
        this.virtualNodesPerShard = virtualNodesPerShard;
        shards.forEach(this::addShard);
    }

    public void addShard(String shard) {
        for (int i = 0; i < virtualNodesPerShard; i++) {
            ring.put(hash(shard + "#" + i), shard);
        }
    }

    public void removeShard(String shard) {
        for (int i = 0; i < virtualNodesPerShard; i++) {
            ring.remove(hash(shard + "#" + i));
        }
    }

    public String get(String key) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("Ring is empty");
        }
        long h = hash(key);
        SortedMap<Long, String> tail = ring.tailMap(h);
        Long nodeHash = tail.isEmpty() ? ring.firstKey() : tail.firstKey();
        return ring.get(nodeHash);
    }

    private long hash(String value) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(value.getBytes(StandardCharsets.UTF_8));
            long h = 0;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (digest[i] & 0xff);
            }
            return h;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
