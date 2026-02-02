package net.pyroneon.NotEnoughBuckets.flows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a lax rate limit, for commonly used webhooks. Applies to all request properties.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RateLimit(name = "lax", capacity = 20, refillAmount = 10, seconds = 60, isGreedy = true)
public @interface LaxRateLimit {}