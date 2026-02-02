package net.pyroneon.NotEnoughBuckets.flows;

import java.lang.annotation.*;

/**
 * An annotation to apply a rate limit to a webhook method. A data structure that represents the amount of traffic
 * some user (tracked by IP or otherwise) is permitted to issue to a specific endpoint within some time frame.
 * See <code>RateLimitHandler</code> for the actual code implementation.
 * <p>
 * This annotation (or a composition of it) can be applied to a method or parent class. Multiple annotations are
 * supported, but if ANY RateLimit is applied at the method level, any class-level RateLimits will be ignored.
 * <p>
 * Do not attempt to apply multiple rate limits against the same property. There is no method of precedence,
 * and they will be applied unpredictably.
 * <p>
 * This annotation cannot be used directly. Instead, a new annotation should be created which composites one
 * or more RateLimit annotations. Second level composite annotations will not work properly.
 * <p>
 * Note that composite annotations must also have a retention policy of <code>RetentionPolicy.RUNTIME</code>,
 * or they won't be recognized by the rate limit handler.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RateLimits.class)
public @interface RateLimit {

    /**
     * The name of this rate limit. This should be unique across different compositions.
     * However, if multiple rate limits are applied on the same composite annotation, they
     * are allowed to share the same name only if their properties don't overlap.
     */
    String name();

    /**
     * The <code>RequestResolver</code> properties this rate limit should apply to.
     * These should be <code>RequestResolver#name()</code> values. If a request property is not
     * specified, it will not be affected by this rate limit.
     * <p>
     * If the array contains the wildcard value "*", then this limit will apply to ALL request properties
     * offered by the <code>RequestResolverManager</code>.
     * <p>
     * Defaults to the wildcard value "*".
     */
    String[] appliesTo() default {"*"};

    /** Max capacity of the token-bucket. Once all are consumed, user must wait for them to refill. */
    int capacity();

    /** Amount of tokens restored per one refill period. */
    int refillAmount();

    /** Time period, in seconds, for one refill. */
    int seconds();

    /**
     * If greedy, refill tokens constantly throughout the refill period.
     * Otherwise, refill the entire amount after one complete period.
     */
    boolean isGreedy();

}