package net.pyroneon.NotEnoughBuckets.tests.integration;

import net.pyroneon.NotEnoughBuckets.buckets.BucketContainer;
import net.pyroneon.NotEnoughBuckets.tests.controllers.ConcurrentController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A shared grouping of tests that should pass regardless of the underlying implementation in use (Redis or Standalone).
 */
@AutoConfigureMockMvc
public abstract class AbstractRateLimitIntegrationTests {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected BucketContainer bucketContainer;

    @AfterEach
    void afterEach() {
        // Clear bucket container in between tests, to maintain isolation.
        bucketContainer.clear();
    }

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

    /**
     * Tests if a bucket will refill after waiting for refill time seconds after exceeding the rate limit.
     */
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

    /**
     * Tests if a class-level rate limit flow will apply to a controller method by default.
     */
    @Test
    void class_level_flow_should_apply_to_method() throws Exception {
        String endpoint = "/test_class_level_1_1_30_false";

        // Ensure calls succeed before the rate limit is met.
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());

        // After the rate limit is met, ensure 429 is returned.
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());
    }

    /**
     * Tests if a class-level rate limit flow will apply to all controller methods by default.
     */
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

    /**
     * Tests if a method-level rate limit flow will override a class-level flow.
     */
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

    /**
     * Tests if the default <code>ip_address</code> resolver will correctly assign different IP addresses their own buckets.
     */
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

    /**
     * Tests if a custom resolver can be applied and used.
     */
    @Test
    void reach_then_exceed_limit_by_custom_resolver() throws Exception {
        String endpoint = "/test_custom_header_1_1_30_false";

        // Set a custom header value and ensure the same bucket is used (rate limit exceeded).
        mockMvc.perform(get(endpoint).header("custom_header","bucket_1")).andExpect(status().isOk());
        mockMvc.perform(get(endpoint).header("custom_header","bucket_1")).andExpect(status().isTooManyRequests());

        // Set a different custom header value to check if a new bucket will be created.
        mockMvc.perform(get(endpoint).header("custom_header","bucket_2")).andExpect(status().isOk());
        mockMvc.perform(get(endpoint).header("custom_header","bucket_2")).andExpect(status().isTooManyRequests());
    }

    /**
     * Tests if a rate limit flow's <code>appliesTo</code> correctly applies it to just the resolver it was configured for.
     */
    @Test
    void ensure_flow_appliesTo_does_not_overlap() throws Exception {
        String endpoint = "/test_custom_header_1_1_30_false";
        String ip_address = "100.0.0.1";

        // Since the endpoint only applies to custom header resolver, it should not rate limit by IP address!
        mockMvc.perform(get(endpoint)
                .header("X-Forwarded-For", ip_address)
                .header("custom_header","bucket_1"))
                .andExpect(status().isOk());
        // Try a different custom header value. If rate limit was being applied by IP, this would fail.
        mockMvc.perform(get(endpoint)
                .header("X-Forwarded-For", ip_address)
                .header("custom_header","bucket_2"))
                .andExpect(status().isOk());
        // Finally confirm the second custom header value was remembered, and rate limit kicks in.
        mockMvc.perform(get(endpoint)
                .header("X-Forwarded-For", ip_address)
                .header("custom_header","bucket_2"))
                .andExpect(status().isTooManyRequests());
    }

    /**
     * Tests if an optional custom resolver will ignore requests when a property is resolved to null.
     */
    @Test
    void test_optional_custom_resolver() throws Exception {
        String endpoint = "/test_custom_header_1_1_30_false";

        // Don't set the custom header value. Rate limit should never be exceeded.
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
    }

    /**
     * Tests if a required custom resolver will fail when request doesn't set the property.
     * The response type is 403 FORBIDDEN by default.
     */
    @Test
    void test_required_custom_resolver() throws Exception {
        String endpoint = "/test_user_id_1_1_30_false";

        // Try not setting the request header "user_id".
        mockMvc.perform(get(endpoint)).andExpect(status().isForbidden());
        // Try again, setting the request header this time.
        mockMvc.perform(get(endpoint).header("user_id", "100")).andExpect(status().isOk());
        mockMvc.perform(get(endpoint).header("user_id", "100")).andExpect(status().isTooManyRequests());
        // Try again, setting a different request header this time.
        mockMvc.perform(get(endpoint).header("user_id", "101")).andExpect(status().isOk());
        mockMvc.perform(get(endpoint).header("user_id", "101")).andExpect(status().isTooManyRequests());
    }

    /**
     * Tests if a flow with multiple resolvers in its <code>appliesTo</code> will create multiple buckets
     * each with the same bucket configurations.
     */
    @Test
    void test_flow_appliesTo_configures_multiple_resolvers() throws Exception {
        String endpoint = "/test_ip_address_custom_header_1_1_30_false";
        String ip_address_1 = "100.0.0.1";
        String ip_address_2 = "100.0.0.2";
        String ip_address_3 = "100.0.0.3";
        String ip_address_4 = "100.0.0.4";

        // Check if the bucket has a capacity of 1 (expected) for IP address bucket. Don't provide a custom header value.
        mockMvc.perform(get(endpoint).header("X-Forwarded-For", ip_address_1)).andExpect(status().isOk());
        mockMvc.perform(get(endpoint).header("X-Forwarded-For", ip_address_1)).andExpect(status().isTooManyRequests());

        // The rate limit should be exceeded for IP 1, regardless of custom header value.
        mockMvc.perform(get(endpoint)
                .header("X-Forwarded-For", ip_address_1)
                .header("custom_header", "something"))
                .andExpect(status().isTooManyRequests());

        // Now try exceeding the rate limit by using the same custom header value, with fresh IPs each time.
        mockMvc.perform(get(endpoint)
                .header("X-Forwarded-For", ip_address_2)
                .header("custom_header", "somethingElse"))
                .andExpect(status().isOk());
        mockMvc.perform(get(endpoint)
                .header("X-Forwarded-For", ip_address_3)
                .header("custom_header", "somethingElse"))
                .andExpect(status().isTooManyRequests());
        mockMvc.perform(get(endpoint)
                .header("X-Forwarded-For", ip_address_4)
                .header("custom_header", "somethingElse"))
                .andExpect(status().isTooManyRequests());
    }

    /**
     * Tests if rate limit exceeded requests will still count towards other properties' buckets.
     * e.g. if a method is 'lax' for IP Addresses, but 'strict' for user IDs.
     * Note that even if one property's rate limit is exceeded, the request still counts towards
     * all properties' buckets.
     */
    @Test
    void test_different_properties_having_different_flows() throws Exception {
        String endpoint = "/test_ip_address_5_1_30_false_user_id_1_1_30_false";
        String ip_address_1 = "100.0.0.1";
        String ip_address_2 = "100.0.0.2";
        String user_id_1 = "101";
        String user_id_2 = "102";
        String user_id_3 = "103";
        String user_id_4 = "104";

        // Users have a stricter flow, while IP addresses have a lenient flow
        // Exceed the rate limit for a user on IP address 1.
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_1))
                .andExpect(status().isOk());
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_1))
                .andExpect(status().isTooManyRequests());

        // Try requesting as the same user, from a new IP
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_2)
                        .header("user_id", user_id_1))
                .andExpect(status().isTooManyRequests());

        // Try requesting as a new user, from the first IP
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_2))
                .andExpect(status().isOk());
        // Exceed the user-specific rate limit
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_2))
                .andExpect(status().isTooManyRequests());

        // Try requesting as a new user, from the first IP
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_3))
                .andExpect(status().isOk());
        // Exceed the user-specific rate limit
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_3))
                .andExpect(status().isTooManyRequests());

        // Using a new user, confirm the IP rate limit has now been exceeded.
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_4))
                .andExpect(status().isTooManyRequests());
        // Confirm the new user has also been personally rate limited by changing IP.
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_2)
                        .header("user_id", user_id_4))
                .andExpect(status().isTooManyRequests());
    }

    /**
     * Tests a custom rate limit flow annotation with one rule.
     */
    @Test
    void test_custom_rate_limit_annotation_single() throws Exception {
        String endpoint = "/test_single_annotation";

        // Ensure the rate limit applies as expected
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        mockMvc.perform(get(endpoint)).andExpect(status().isTooManyRequests());
    }

    /**
     * Tests a composite rate limit flow annotation that applies multiple flows based on property.
     */
    @Test
    void test_custom_rate_limit_annotation_composite() throws Exception {
        String endpoint = "/test_composite_annotation";
        String ip_address_1 = "100.0.0.1";
        String ip_address_2 = "100.0.0.2";
        String user_id_1 = "101";
        String user_id_2 = "102";
        String user_id_3 = "103";

        // Users have a stricter flow, while IP addresses have a lenient flow
        // Exceed the rate limit for a user on IP address 1.
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_1))
                .andExpect(status().isOk());
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_1))
                .andExpect(status().isTooManyRequests());

        // Try requesting as the same user, from a new IP
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_2)
                        .header("user_id", user_id_1))
                .andExpect(status().isTooManyRequests());

        // Try requesting as a new user, from the first IP
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_2))
                .andExpect(status().isOk());
        // Exceed the IP rate limit with a new user
        mockMvc.perform(get(endpoint)
                        .header("X-Forwarded-For", ip_address_1)
                        .header("user_id", user_id_3))
                .andExpect(status().isTooManyRequests());
    }

    /**
     * Tests if a rate limit that applies to an invalid property name returns a 500 INTERNAL_SERVER_ERROR.
     */
    @Test
    void test_invalid_property_name_misconfiguration() throws Exception {
        String endpoint = "/test_invalid_property_name";

        // Ensure the request fails and a 500 is returned to indicate misconfiguration.
        mockMvc.perform(get(endpoint)).andExpect(status().isInternalServerError());
    }

    /**
     * Issues multiple concurrent requests and ensures the rate limit is not exceeded.
     */
    @Test
    void test_concurrent_rate_limit_exceeded() throws Exception {
        String endpoint = "/test_concurrent_10_1_30_false";

        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await(); // ensure simultaneous start

                    int status = mockMvc.perform(get(endpoint))
                            .andReturn()
                            .getResponse()
                            .getStatus();

                    statuses.add(status);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();   // wait until all threads are ready
        start.countDown(); // release them simultaneously
        done.await();    // wait for completion

        long success = statuses.stream().filter(s -> s == 200).count();
        long limited = statuses.stream().filter(s -> s == 429).count();

        assertEquals(10, success);
        assertEquals(10, limited);

        // Ensure that more than 1 request was being processed concurrently
        assertTrue(ConcurrentController.maxActive.get() > 1);

        executor.shutdown();
    }
}