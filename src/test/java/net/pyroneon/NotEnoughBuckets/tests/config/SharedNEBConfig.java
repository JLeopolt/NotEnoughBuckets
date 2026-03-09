package net.pyroneon.NotEnoughBuckets.tests.config;

import net.pyroneon.NotEnoughBuckets.components.RequestResolverManager;
import net.pyroneon.NotEnoughBuckets.tests.resolvers.CustomHeaderResolver;
import net.pyroneon.NotEnoughBuckets.tests.resolvers.UserIdResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedNEBConfig {

    @Autowired
    public SharedNEBConfig(RequestResolverManager resolverManager) {
        resolverManager.addResolver(new UserIdResolver());
        resolverManager.addResolver(new CustomHeaderResolver());
    }
}
