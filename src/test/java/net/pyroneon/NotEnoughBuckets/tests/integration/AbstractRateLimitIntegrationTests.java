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
    void reach_then_exceed_limit() throws Exception {
        String endpoint = "/test_5_1_30_false";
        int bucketCapacity = 5;

        // Ensure calls succeed before the rate limit is met.
        for (int i = 0; i < bucketCapacity; i++) {
            mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        }

        // After the rate limit is met, ensure 429 is returned.
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());
    }

    @Test
    void reach_and_await_bucket_refill() throws Exception {
        String endpoint = "/test_1_1_1_true";
        int bucketRefillTime = 1; // in seconds

        // Ensure calls succeed before the rate limit is met.
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());

        // After the rate limit is met, ensure 429 is returned.
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());

        // Wait for the bucket to refill. Add extra time just in case.
        Thread.sleep(bucketRefillTime * 1000 + 500);

        // The bucket should have refilled 1 token.
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());

        // After the rate limit is met, ensure 429 is returned.
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());
    }

    @Test
    void class_level_flow_should_apply_to_method() throws Exception {
        String endpoint = "/test_class_level_1_1_30_false";

        // Ensure calls succeed before the rate limit is met.
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());

        // After the rate limit is met, ensure 429 is returned.
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());
    }

    @Test
    void class_level_flow_methods_should_not_share_a_bucket() throws Exception {
        String endpoint = "/test_class_level_1_1_30_false";
        String endpoint2 = "/test_2_class_level_1_1_30_false";

        // Reach the limit on endpoint 1
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());

        // The limit on endpoint 2 should not be reached.
        // Although they share the same flow, the two endpoints have different buckets.
        mockMvc.perform(get(endpoint2)).andExpect(status().isOk());
        mockMvc.perform(get(endpoint2)).andExpect(status().isTooManyRequests());
    }

    @Test
    void method_level_flow_should_override_class_level() throws Exception {
        String endpoint = "/test_method_level_override_5_1_30_false";
        int bucketCapacity = 5;

        // Ensure calls succeed before the rate limit is met.
        for (int i = 0; i < bucketCapacity; i++) {
            mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        }

        // After the rate limit is met, ensure 429 is returned.
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());
    }

    @Test
    void ensure_separate_buckets_per_ip_address() throws Exception {
        String endpoint = "/test_1_1_1_true";
        String ip_address_1 = "100.0.0.1";
        String ip_address_2 = "100.0.0.2";

        // Try exceeding the rate limit from one IP address.
        mockMvc.perform(get(endpoint).header("X-Forwarded-For",ip_address_1))
                .andExpect(status().isOk());
        mockMvc.perform(get(endpoint).header("X-Forwarded-For", ip_address_1))
                .andExpect(status().isTooManyRequests());

        // Now, ensure a different IP address gets its own fresh bucket.
        mockMvc.perform(get(endpoint).header("X-Forwarded-For",ip_address_2))
                .andExpect(status().isOk());
        mockMvc.perform(get(endpoint).header("X-Forwarded-For", ip_address_2))
                .andExpect(status().isTooManyRequests());
    }
}