package net.pyroneon.NotEnoughBuckets.tests.controllers;

import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RateLimit(name = "test_5_1_30_false", capacity = 5, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test")
    public String test() {
        return "ok";
    }
}
