package net.pyroneon.NotEnoughBuckets.enablers;

import net.pyroneon.NotEnoughBuckets.config.StandaloneConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables the rate limiting system, using the standalone configuration. This configuration will not support
 * synchronization across multiple instances, but won't require a redis server.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(StandaloneConfig.class)
public @interface EnableRateLimitingStandalone {}
