package net.pyroneon.NotEnoughBuckets.buckets;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A <code>BucketContainer</code> which uses Redis to persist buckets in a distributed setting.
 * This container specifically uses proxied buckets, which automatically sync with the Redis backend, making
 * them suitable for use in distributed applications.
 */
public class RedisBucketContainer implements BucketContainer {

    private static final String KEY_PREFIX = "neb:bucket:";
    private final JedisPool jedisPool;
    private final ProxyManager<byte[]> proxyManager;

    public RedisBucketContainer(JedisPool jedisPool, ProxyManager<byte[]> proxyManager) {
        this.jedisPool = jedisPool;
        this.proxyManager = proxyManager;
    }

    /**
     * Deletes all existing buckets.
     */
    @Override
    public void clear() {
        // Delete all keys with the NEB prefix.
        Jedis jedis = jedisPool.getResource();
        Set<String> keys = jedis.keys(KEY_PREFIX + "*");
        if(!keys.isEmpty()){
            jedis.del(keys.toArray(new String[0]));
        }
    }

    /**
     * Retrieves an existing bucket or creates a new bucket with the given configuration.
     * This method will return a proxied bucket, meaning any operation done to it will be reflected in Redis automatically.
     * @param key                   The key to resolve/store this bucket from/to. Should be unique to the request type and issuer.
     *                              An NEB-specific prefix will automatically be applied to this key.
     * @param configurationSupplier A producer function which returns the configuration to use,
     *                              in the event a new bucket must be created.
     * @return The proxied bucket associated with this key. If no bucket exists, a new one is instantiated.
     */
    @Override
    @NotNull
    public BucketProxy resolve(String key, Supplier<BucketConfiguration> configurationSupplier) {
        // Create the redis key based on composite key and prefix, then convert it to bytes.
        String finalKey = KEY_PREFIX + key;
        byte[] redisKeyBytes = finalKey.getBytes(StandardCharsets.UTF_8);
        // Get the bucket if it exists, or create a new one with the given supplier.
        return proxyManager.builder().build(redisKeyBytes, configurationSupplier);
    }
}
