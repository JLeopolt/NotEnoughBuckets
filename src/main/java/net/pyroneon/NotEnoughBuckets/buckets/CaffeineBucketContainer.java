package net.pyroneon.NotEnoughBuckets.buckets;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.MathType;
import io.github.bucket4j.TimeMeter;
import io.github.bucket4j.local.LockFreeBucket;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * An <code>BucketContainer</code> which uses Caffeine as a cached internal memory map to store buckets.
 */
public class CaffeineBucketContainer implements BucketContainer {

    // Caffeine uses an internal ConcurrentHashMap to handle caching Rate Limits.
    private final Cache<String, Bucket> cache;

    public CaffeineBucketContainer() {
        cache = Caffeine.newBuilder()
                // Set a hard reset after some period of time. This should hopefully be
                // larger than the max period of time a bucket takes to fully refill.
                .expireAfterWrite(Duration.ofMinutes(2))
                .build();
    }

    /**
     * Retrieves an existing bucket or creates a new bucket with the given configuration.
     * Returns an in-memory, mutable Bucket.
     * @param key                   The key to resolve/store this bucket from/to. Should be unique to the request type and issuer.
     * @param configurationSupplier A producer function which returns the configuration to use,
     *                              in the event a new bucket must be created.
     * @return The bucket associated with this key. If no bucket exists, a new one is instantiated.
     */
    @Override
    @NotNull
    public Bucket resolve(String key, Supplier<BucketConfiguration> configurationSupplier) {
        // Check if the bucket already exists, if so, return it.
        // Otherwise, create and return a new bucket w/ the given configuration from supplier.
        return cache.get(key, k -> new LockFreeBucket(
                // Manually build a LockFree bucket, to use the configuration directly.
                configurationSupplier.get(),
                // Use default settings.
                MathType.INTEGER_64_BITS,
                TimeMeter.SYSTEM_MILLISECONDS)
        );
    }
}
