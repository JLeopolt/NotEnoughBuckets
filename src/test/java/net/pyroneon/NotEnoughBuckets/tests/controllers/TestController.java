package net.pyroneon.NotEnoughBuckets.tests.controllers;

import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RateLimit(name = "test_5_1_30_false", capacity = 5, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_5_1_30_false")
    public String test_5_1_30_false() {
        return "ok";
    }

    @RateLimit(name = "test_1_1_1_true", capacity = 1, refillAmount = 1, seconds = 1, isGreedy = true)
    @GetMapping("/test_1_1_1_true")
    public String test_1_1_1_true() {
        return "ok";
    }
}
