package net.pyroneon.NotEnoughBuckets.tests.controllers;

import net.pyroneon.NotEnoughBuckets.tests.flows.CompositeRateLimit;
import net.pyroneon.NotEnoughBuckets.tests.flows.SingleRateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomAnnotationController {

    @SingleRateLimit
    @GetMapping("/test_single_annotation")
    public String test_single_annotation() {
        return "ok";
    }

    @CompositeRateLimit
    @GetMapping("/test_composite_annotation")
    public String test_composite_annotation() {
        return "ok";
    }
}
