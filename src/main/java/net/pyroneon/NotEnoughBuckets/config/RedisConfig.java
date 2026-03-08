package net.pyroneon.NotEnoughBuckets.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import net.pyroneon.NotEnoughBuckets.buckets.BucketContainer;
import net.pyroneon.NotEnoughBuckets.buckets.RedisBucketContainer;
import net.pyroneon.NotEnoughBuckets.config.common.SharedConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import redis.clients.jedis.JedisPool;

import java.time.Duration;

/**
 * Configures rate limiting with Redis support. Assumes Redis is enabled by the application.
 */
@Configuration
// Include common configurations
@Import(SharedConfig.class)
public class RedisConfig {

    /** The bucket container (repository) to use for rate limits. */
    @Bean
    public BucketContainer preferredBucketContainer(JedisPool jedisPool, ProxyManager<byte[]> proxyManager) {
        return new RedisBucketContainer(jedisPool, proxyManager);
    }

    /** This proxy manager will allow direct interactions with Bucket4J buckets stored in Redis. */
    @Bean
    public ProxyManager<byte[]> bucket4jProxyManager(JedisPool jedisPool) {
        // Create custom configuration for the proxy manager
        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault();
        // Set a default expiration.
        clientSideConfig = clientSideConfig.withExpirationAfterWriteStrategy(
                // After a bucket is fully refilled, wait X seconds before resetting (deleting) it.
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)));
        // Build the proxy manager with provided configurations and jedis access.
        return JedisBasedProxyManager.builderFor(jedisPool)
                .withClientSideConfig(clientSideConfig).build();
    }

}
