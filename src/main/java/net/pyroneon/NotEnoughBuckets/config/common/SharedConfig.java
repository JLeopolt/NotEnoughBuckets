package net.pyroneon.NotEnoughBuckets.config.common;

import net.pyroneon.NotEnoughBuckets.buckets.BucketContainer;
import net.pyroneon.NotEnoughBuckets.components.RateLimitHandler;
import net.pyroneon.NotEnoughBuckets.components.RateLimitService;
import net.pyroneon.NotEnoughBuckets.components.RequestResolverManager;
import net.pyroneon.NotEnoughBuckets.resolvers.IpAddressResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Defines shared configurations that should be applied regardless of the specific config selected.
 * These beans should not be overwritten or replaced by the application.
 */
@Configuration
@ComponentScan
public class SharedConfig {

    /**
     * The manager to use when resolving request properties. This should not be extended or replaced by the
     * application. Instead, custom request resolvers can be given to the manager, which will use them automatically.
     */
    @Bean
    public RequestResolverManager defaultRequestResolverManager() {
        // By default, use just an IP resolver.
        return new RequestResolverManager(new IpAddressResolver());
    }

    @Bean
    public RateLimitService rateLimitService(BucketContainer bucketContainer, RequestResolverManager requestResolverManager) {
        return new RateLimitService(bucketContainer, requestResolverManager);
    }

    @Bean
    public RateLimitHandler rateLimitHandler(RateLimitService rateLimitService) {
        return new RateLimitHandler(rateLimitService);
    }
}
