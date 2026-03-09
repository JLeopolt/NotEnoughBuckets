package net.pyroneon.NotEnoughBuckets.tests.controllers;

import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import net.pyroneon.NotEnoughBuckets.resolvers.IpAddressResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MethodLevelController {

    @RateLimit(name = "test_5_1_30_false", appliesTo = IpAddressResolver.PROPERTY_NAME, capacity = 5, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_5_1_30_false")
    public String test_5_1_30_false() {
        return "ok";
    }

    @RateLimit(name = "test_1_1_1_true", appliesTo = IpAddressResolver.PROPERTY_NAME, capacity = 1, refillAmount = 1, seconds = 1, isGreedy = true)
    @GetMapping("/test_1_1_1_true")
    public String test_1_1_1_true() {
        return "ok";
    }

    @RateLimit(name = "test_invalid_property_name", appliesTo = "a_random_non_existent_property_name", capacity = 1, refillAmount = 1, seconds = 1, isGreedy = true)
    @GetMapping("/test_invalid_property_name")
    public String test_invalid_property_name() {
        return "ok";
    }
}
