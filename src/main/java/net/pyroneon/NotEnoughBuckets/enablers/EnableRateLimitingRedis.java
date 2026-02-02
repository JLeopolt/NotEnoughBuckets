package net.pyroneon.NotEnoughBuckets.enablers;

import net.pyroneon.NotEnoughBuckets.config.RedisConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables the rate limiting system, using the standalone configuration. This configuration supports
 * synchronization across multiple instances, and depends on the Redis system.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RedisConfig.class)
public @interface EnableRateLimitingRedis {}
