package net.pyroneon.NotEnoughBuckets.tests.controllers;

import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import net.pyroneon.NotEnoughBuckets.resolvers.IpAddressResolver;
import net.pyroneon.NotEnoughBuckets.tests.flows.CompositeRateLimit;
import net.pyroneon.NotEnoughBuckets.tests.flows.SingleRateLimit;
import net.pyroneon.NotEnoughBuckets.tests.resolvers.CustomHeaderResolver;
import net.pyroneon.NotEnoughBuckets.tests.resolvers.UserIdResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomResolverController {

    @RateLimit(name = "test_custom_header_1_1_30_false", appliesTo = CustomHeaderResolver.PROPERTY_NAME,
            capacity = 1, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_custom_header_1_1_30_false")
    public String test_custom_header_1_1_30_false() {
        return "ok";
    }

    @RateLimit(name = "test_user_id_1_1_30_false", appliesTo = UserIdResolver.PROPERTY_NAME,
            capacity = 1, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_user_id_1_1_30_false")
    public String test_user_id_1_1_30_false() {
        return "ok";
    }

    @RateLimit(name = "test_ip_address_custom_header_1_1_30_false", appliesTo = {CustomHeaderResolver.PROPERTY_NAME, IpAddressResolver.PROPERTY_NAME},
            capacity = 1, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_ip_address_custom_header_1_1_30_false")
    public String test_ip_address_custom_header_1_1_30_false() {
        return "ok";
    }

    @RateLimit(name = "test_ip_address_user_id_1_1_30_false", appliesTo = {UserIdResolver.PROPERTY_NAME, IpAddressResolver.PROPERTY_NAME},
            capacity = 1, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_ip_address_user_id_1_1_30_false")
    public String test_ip_address_user_id_1_1_30_false() {
        return "ok";
    }

    @RateLimit(name = "test_ip_address_5_1_30_false", appliesTo = IpAddressResolver.PROPERTY_NAME,
            capacity = 5, refillAmount = 1, seconds = 30, isGreedy = false)
    @RateLimit(name = "test_user_id_1_1_30_false", appliesTo = UserIdResolver.PROPERTY_NAME,
            capacity = 1, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_ip_address_5_1_30_false_user_id_1_1_30_false")
    public String test_ip_address_3_1_30_false_user_id_1_1_30_false() {
        return "ok";
    }
}
