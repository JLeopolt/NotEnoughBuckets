package net.pyroneon.NotEnoughBuckets.tests.integration;

import net.pyroneon.NotEnoughBuckets.config.StandaloneConfig;
import net.pyroneon.NotEnoughBuckets.tests.TestApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TestApplication.class, StandaloneConfig.class})
public class StandaloneRateLimitIntegrationTests extends AbstractRateLimitIntegrationTests {

}