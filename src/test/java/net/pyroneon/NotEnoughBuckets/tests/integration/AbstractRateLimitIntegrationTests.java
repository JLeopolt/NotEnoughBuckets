package net.pyroneon.NotEnoughBuckets.tests.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A shared grouping of tests that should pass regardless of the underlying implementation in use (Redis or Standalone).
 */
@AutoConfigureMockMvc
public abstract class AbstractRateLimitIntegrationTests {

    @Autowired protected MockMvc mockMvc;

    /**
     * Tests if a controller-method rate limit is applied correctly by invoking an endpoint
     * N times, where N is the capacity of the bucket for the given rate limit profile.
     * Expects the request to return 200 OK the first N-1 times, then return 429 TOO MANY REQUESTS the Nth time.
     */
    @Test
    void shouldRateLimitAfterThreshold() throws Exception {
        String endpoint = "/test";
        int bucketCapacity = 5;

        // Ensure calls succeed before the rate limit is met.
        for (int i = 0; i < bucketCapacity; i++) {
            mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        }

        // After the rate limit is met, ensure 429 is returned.
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());
    }
}