package net.pyroneon.NotEnoughBuckets.buckets;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * An interface for mapping keys to buckets. Can be implemented as an in-memory solution like <code>ConcurrentHashMap</code>,
 * or a distributed solution like Redis. Any implementation is assumed to be thread-safe. Uses the requester's IP Address
 * and endpoint method as a composite key, mapped to a Bucket4J bucket.
 */
public interface BucketContainer {

    /**
     * Retrieves an existing bucket or creates a new bucket with the given configuration.
     * The resultant bucket can either be an in-memory bucket or a proxy, depending on implementation.
     * Either way, the bucket is expected to be mutable.
     *
     * @param key The key to resolve/store this bucket from/to. Should be unique to the request type and issuer.
     *
     * @param configurationSupplier A producer function which returns the configuration to use,
     *                              in the event a new bucket must be created.
     *
     * @return The bucket associated with this key. If no bucket exists, a new one is instantiated. Must be mutable.
     */
    @NotNull
    Bucket resolve(String key, Supplier<BucketConfiguration> configurationSupplier);

}
