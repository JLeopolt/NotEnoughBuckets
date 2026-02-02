package net.pyroneon.NotEnoughBuckets.components;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import net.pyroneon.NotEnoughBuckets.buckets.BucketContainer;
import net.pyroneon.NotEnoughBuckets.exceptions.RateLimitExceededException;
import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Offers methods to directly set and interact with rate limits. Useful for manually creating rate limits when
 * a non-controller endpoint needs to be rate limited.
 */
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    private final BucketContainer bucketContainer;

    private final RequestResolverManager requestResolverManager;

    // Used to cache bucket configurations, to avoid recomputing values.
    private final Map<String, BucketConfiguration> bucketConfigurationCache;

    /**
     * @param bucketContainer The repository to use.
     * @param requestResolverManager The request resolver manager to use.
     */
    public RateLimitService(BucketContainer bucketContainer, RequestResolverManager requestResolverManager){
        this.bucketContainer = bucketContainer;
        this.requestResolverManager = requestResolverManager;
        this.bucketConfigurationCache = new HashMap<>();
    }

    /**
     * Applies the rate limit by first pulling request's resolved properties then comparing them against the provided
     * rate limits (if any). Updates the contents of bucket(s) to increment the short term 'history' of usage by this requester.
     * If the rate limit is exceeded, throws an exception, expecting the invoking method to gracefully fail.
     * @param request The request.
     * @param methodIdentifier A unique identifier for this method. Can be the qualified method name,
     *                         the endpoint and HTTP method, or something else. Every subsequent call to this endpoint,
     *                         with the same HTTP method should use the same identifier (consistency).
     * @param rateLimits Any rate limits to apply. Should be consistent per method. May be empty.
     * @throws RateLimitExceededException If one or more rate limits were exceeded by this request.
     */
    public void apply(@NotNull HttpServletRequest request, @NotNull String methodIdentifier, List<RateLimit> rateLimits) {
        // If no rate limits, do nothing.
        if(rateLimits.isEmpty()) {
            // If a method doesn't have any rate limits for some reason. (Unexpected)
            logger.warn("Potential misconfiguration: method '{}' doesn't have any rate limits.", methodIdentifier);
            return;
        }

        logger.trace("Rate limited method: '{}' was invoked with {} effective rate limits.", methodIdentifier, rateLimits.size());

        // Resolve all required properties from the request. (Shared by all effective rate limits)
        Map<String, String> resolvedProperties = requestResolverManager.mapResolvedProperties(request);

        // Iterate thru each effective rate limit.
        boolean isRateLimitExceeded = false;
        for(RateLimit rateLimit : rateLimits) {
            logger.trace("Applying rate limit '{}'.", rateLimit.name());

            // Get the properties this rate limit applies to.
            HashSet<String> appliesTo = new HashSet<>(List.of(rateLimit.appliesTo()));
            // If the rate limit contains the wildcard, apply for all properties.
            boolean isWildcard = appliesTo.contains("*");

            // Individually handle each required property.
            for(Map.Entry<String, String> property : resolvedProperties.entrySet()) {

                // First, check if this rate limit had a wildcard.
                // If not, check if it applies to this property.
                if(!isWildcard && !appliesTo.contains(property.getKey())) {
                    // If the property doesn't apply to this rate limit, skip.
                    continue;
                }

                // Create a composite key for this endpoint, with the given property
                String compositeKey = createCompositeKey(methodIdentifier, property.getKey(), property.getValue());

                // Retrieve the bucket associated with this key (if one exists)
                // If no bucket exists yet, creates a fresh one.
                Bucket bucket = bucketContainer.resolve(compositeKey,
                        // The supplier uses an internal cache to avoid rebuilding configurations.
                        () -> getOrCreateBucketConfiguration(rateLimit));

                // Try consuming a token from the bucket. Recall that buckets are assumed to be mutable.
                if(bucket.tryConsume(1)) {
                    logger.trace("Request property '{}' didn't exceed rate limit.", property.getKey());
                } else {
                    // Otherwise, mark the rate limit as exceeded; but continue looping.
                    isRateLimitExceeded = true;
                    logger.trace("Request property '{}' exceeded rate limit.", property.getKey());
                }
            }
        }

        // Once all properties across all rate limits have been processed,
        // check if any rate limit was ever exceeded.
        if(isRateLimitExceeded) throw new RateLimitExceededException();

        // If no rate limit was ever exceeded, success.
    }

    /**
     * Creates a composite key that uniquely identifies the endpoint used and issuer.
     * @param qualifiedMethod The qualified method. This should be unique to the application.
     * @param propertyName Name of the resolved property.
     * @param propertyValue Value of the resolved property.
     * @return A composite key, which is unique to the endpoint and issuer (based on the resolved property).
     */
    @NotNull
    private String createCompositeKey(String qualifiedMethod, String propertyName, String propertyValue) {
        return "method:" + qualifiedMethod + ":property:" + propertyName + "=" + propertyValue;
    }

    /**
     * Gets the <code>BucketConfiguration</code> to use for this specific <code>RateLimit</code>.
     * Uses an internal cache to avoid rebuilding identical configurations.
     * @param rateLimit The rate limit to apply.
     * @return The rate limit's corresponding <code>BucketConfiguration</code>.
     */
    private BucketConfiguration getOrCreateBucketConfiguration(RateLimit rateLimit) {
        // Check if a configuration was already cached by comparing against its name.
        BucketConfiguration configuration = bucketConfigurationCache.get(rateLimit.name());
        if(configuration != null) return configuration;
        // If no cached value exists, instantiate a new configuration now.
        configuration = createBucketConfiguration(rateLimit);
        // Cache this new configuration, then return it.
        bucketConfigurationCache.put(rateLimit.name(), configuration);
        return configuration;
    }

    /**
     * Creates a new <code>BucketConfiguration</code> based on the <code>RateLimit</code> annotation provided.
     * @param rateLimit The <code>RateLimit</code> annotation which was applied to the method.
     * @return The corresponding <code>BucketConfiguration</code>.
     */
    private static BucketConfiguration createBucketConfiguration(RateLimit rateLimit) {
        // Use the rate limit's properties to configure the bucket.
        var builder = Bandwidth.builder().capacity(rateLimit.capacity());
        Bandwidth bandwidth;
        // If rate limit is greedy
        if(rateLimit.isGreedy()) {
            bandwidth = builder.refillGreedy(
                    rateLimit.refillAmount(),
                    Duration.ofSeconds(rateLimit.seconds())
            ).build();
            // If rate limit isn't greedy
        } else {
            bandwidth = builder.refillIntervally(
                    rateLimit.refillAmount(),
                    Duration.ofSeconds(rateLimit.seconds())
            ).build();
        }
        // Complete the bucket, then get its configuration for reuse.
        return Bucket.builder().addLimit(bandwidth).build().getConfiguration();
    }

}
