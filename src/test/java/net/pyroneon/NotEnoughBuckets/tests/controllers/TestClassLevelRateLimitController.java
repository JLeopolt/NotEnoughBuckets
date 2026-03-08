package net.pyroneon.NotEnoughBuckets.tests.controllers;

import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RateLimit(name = "test_class_level_1_1_30_false", capacity = 1, refillAmount = 1, seconds = 30, isGreedy = false)
public class TestClassLevelRateLimitController {

    @GetMapping("/test_class_level_1_1_30_false")
    public String test_class_level_1_1_30_false() {
        return "ok";
    }

    @GetMapping("/test_2_class_level_1_1_30_false")
    public String test_2_class_level_1_1_30_false() {
        return "ok";
    }

    @RateLimit(name = "test_method_level_override_5_1_30_false", capacity = 5, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_method_level_override_5_1_30_false")
    public String test_method_level_override_5_1_30_false() {
        return "ok";
    }
}
