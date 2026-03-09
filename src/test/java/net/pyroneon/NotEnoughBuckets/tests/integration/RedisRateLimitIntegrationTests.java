package net.pyroneon.NotEnoughBuckets.tests.integration;

import com.redis.testcontainers.RedisContainer;
import net.pyroneon.NotEnoughBuckets.config.RedisConfig;
import net.pyroneon.NotEnoughBuckets.tests.TestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootTest(classes = {TestApplication.class, RedisConfig.class, RedisRateLimitIntegrationTests.Config.class})
@Testcontainers
public class RedisRateLimitIntegrationTests extends AbstractRateLimitIntegrationTests {

    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7"))
            .withCommand("redis-server --requirepass testpass")
            .waitingFor(Wait.forListeningPort());

    @BeforeAll
    public static void beforeAll() {
        redis.start();
    }

    @TestConfiguration
    static class Config {
        /** Used when creating a <code>ProxyManager</code> for Bucket4J when rate limiting. */
        @Bean
        public JedisPool jedisPool() {
            // Redis configuration
            String host = redis.getHost();
            int port = redis.getFirstMappedPort();
            String password = "testpass";
            boolean ssl = false;

            // Initialize a Jedis pool
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(8);
            jedisPoolConfig.setMaxIdle(8);
            jedisPoolConfig.setMinIdle(1);
            jedisPoolConfig.setTestOnBorrow(true);

            // Set a higher timeout to give a grace period during CI runs.
            JedisPool jedisPool =  new JedisPool(jedisPoolConfig, host, port, 10000, password, ssl);

            // Warm up the pool to avoid flakiness in first test
            try(Jedis jedis = jedisPool.getResource()) {
                jedis.auth(password);
                jedis.ping();
            }
            return jedisPool;
        }
    }
}