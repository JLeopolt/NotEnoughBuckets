package net.pyroneon.NotEnoughBuckets.tests.integration;

import com.redis.testcontainers.RedisContainer;
import net.pyroneon.NotEnoughBuckets.config.RedisConfig;
import net.pyroneon.NotEnoughBuckets.tests.TestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootTest(classes = {TestApplication.class, RedisConfig.class})
@Testcontainers
public class RedisRateLimitIntegrationTests extends AbstractRateLimitIntegrationTests {

    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7"))
            .withCommand("redis-server --requirepass testpass");

    @BeforeAll
    public static void beforeAll() {
        redis.start();
    }

    @Configuration
    static class Config {
        /** Used when creating a <code>ProxyManager</code> for Bucket4J when rate limiting. */
        @Bean
        public JedisPool jedisPool() {
            String host = redis.getHost();
            int port = redis.getFirstMappedPort();
            String password = "testpass";
            boolean ssl = false;
            // Initialize a Jedis pool
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            return new JedisPool(jedisPoolConfig, host, port, 2000, password, ssl);
        }
    }
}