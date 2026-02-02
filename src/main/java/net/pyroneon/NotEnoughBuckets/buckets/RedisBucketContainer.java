package net.pyroneon.NotEnoughBuckets.buckets;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * A <code>BucketContainer</code> which uses Redis to persist buckets in a distributed setting.
 * This container specifically uses proxied buckets, which automatically sync with the Redis backend, making
 * them suitable for use in distributed applications.
 */
public class RedisBucketContainer implements BucketContainer {

    private final ProxyManager<byte[]> proxyManager;

    public RedisBucketContainer(ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    /**
     * Retrieves an existing bucket or creates a new bucket with the given configuration.
     * This method will return a proxied bucket, meaning any operation done to it will be reflected in Redis automatically.
     * @param key                   The key to resolve/store this bucket from/to. Should be unique to the request type and issuer.
     * @param configurationSupplier A producer function which returns the configuration to use,
     *                              in the event a new bucket must be created.
     * @return The proxied bucket associated with this key. If no bucket exists, a new one is instantiated.
     */
    @Override
    @NotNull
    public BucketProxy resolve(String key, Supplier<BucketConfiguration> configurationSupplier) {
        // Create the redis key based on composite key, then convert it to bytes.
        byte[] redisKeyBytes = key.getBytes(StandardCharsets.UTF_8);
        // Get the bucket if it exists, or create a new one with the given supplier.
        return proxyManager.builder().build(redisKeyBytes, configurationSupplier);
    }
}
