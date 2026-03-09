package net.pyroneon.NotEnoughBuckets.tests.flows;

import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import net.pyroneon.NotEnoughBuckets.resolvers.IpAddressResolver;
import net.pyroneon.NotEnoughBuckets.tests.resolvers.UserIdResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RateLimit(name = "composite_test_ip_address_3_1_30_false", appliesTo = IpAddressResolver.PROPERTY_NAME,
        capacity = 3, refillAmount = 1, seconds = 30, isGreedy = false)
@RateLimit(name = "composite_test_user_id_1_1_30_false", appliesTo = UserIdResolver.PROPERTY_NAME,
        capacity = 1, refillAmount = 1, seconds = 30, isGreedy = false)
public @interface CompositeRateLimit {}
