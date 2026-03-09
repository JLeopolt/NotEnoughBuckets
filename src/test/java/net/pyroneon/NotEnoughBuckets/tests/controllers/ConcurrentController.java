package net.pyroneon.NotEnoughBuckets.tests.controllers;

import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import net.pyroneon.NotEnoughBuckets.resolvers.IpAddressResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ConcurrentController {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentController.class);

    public static AtomicInteger active = new AtomicInteger();
    public static AtomicInteger maxActive = new AtomicInteger();

    @RateLimit(name = "test_concurrent_10_1_30_false", appliesTo = IpAddressResolver.PROPERTY_NAME, capacity = 10, refillAmount = 1, seconds = 30, isGreedy = false)
    @GetMapping("/test_concurrent_10_1_30_false")
    public String test_concurrent_10_1_30_false() throws InterruptedException {
        logger.trace("Concurrent request handled by thread '{}'.", Thread.currentThread().getName());
        int current = active.incrementAndGet();
        maxActive.updateAndGet(m -> Math.max(m, current));
        Thread.sleep(200);
        active.decrementAndGet();
        return "ok";
    }
}
