package net.pyroneon.NotEnoughBuckets.flows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a strict rate limit, for more expensive webhooks. Applies to all request properties.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RateLimit(name = "strict", capacity = 5, refillAmount = 5, seconds = 120, isGreedy = true)
public @interface StrictRateLimit {}