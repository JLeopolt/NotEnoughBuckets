package net.pyroneon.NotEnoughBuckets.config.common;


import net.pyroneon.NotEnoughBuckets.components.RateLimitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** A wrapper configuration that depends on a RateLimitHandler and applies it as a WebMvc interceptor. */
@Configuration
public class RateLimitRegistry implements WebMvcConfigurer {

    private final RateLimitHandler rateLimitHandler;

    @Autowired
    public RateLimitRegistry(RateLimitHandler rateLimitHandler) {
        this.rateLimitHandler = rateLimitHandler;
    }

    /** Register the rate limit handler as an interceptor. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitHandler);
    }
}