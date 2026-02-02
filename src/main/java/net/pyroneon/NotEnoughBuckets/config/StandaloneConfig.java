package net.pyroneon.NotEnoughBuckets.config;

import net.pyroneon.NotEnoughBuckets.buckets.BucketContainer;
import net.pyroneon.NotEnoughBuckets.buckets.CaffeineBucketContainer;
import net.pyroneon.NotEnoughBuckets.config.common.SharedConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configures rate limiting in a standalone environment (non-distributed).
 */
@Configuration
// Include common configurations
@Import(SharedConfig.class)
public class StandaloneConfig {

    /** The container (repository) to use for rate limits. */
    @Bean
    public BucketContainer preferredBucketContainer() {
        return new CaffeineBucketContainer();
    }
}
