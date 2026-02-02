package net.pyroneon.NotEnoughBuckets.components;

import jakarta.servlet.http.HttpServletRequest;
import net.pyroneon.NotEnoughBuckets.exceptions.RateLimitPropertyResolutionFailure;
import net.pyroneon.NotEnoughBuckets.resolvers.RequestResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Attempts to extract properties from an incoming HTTP request based on a configured set of resolvers.
 * Custom resolvers can be added or removed from the default internal resolvables list.
 */
public class RequestResolverManager {
    private static final Logger logger = LoggerFactory.getLogger(RequestResolverManager.class);

    // The resolvers to use. This should be a modifiable list type.
    @NotNull private final List<RequestResolver> requestResolvers;

    public RequestResolverManager(RequestResolver... requestResolvers) {
        // Ensure the list remains mutable, so clients can add custom resolvers as needed.
        this.requestResolvers = new ArrayList<>();
        this.requestResolvers.addAll(List.of(requestResolvers));
    }

    /**
     * Runs and aggregates results of all resolvers on the request, maps them
     * by the name of the property -> the request's value for that property.
     * Note that if a property isn't required and is missing, no entry for it will exist in the resulting map.
     */
    @NotNull
    public Map<String, String> mapResolvedProperties(HttpServletRequest request) {
        Map<String, String> resolvedProperties = new TreeMap<>();
        for(RequestResolver requestResolver : requestResolvers) {
            // Resolve the value of the property
            String value = requestResolver.resolve(request);
            if(value != null) {
                // Map the name of the property to the resolved value.
                resolvedProperties.put(requestResolver.getPropertyName(), value);
                logger.trace("Resolved property '{}'='{}' in request.", requestResolver.getPropertyName(), value);
            } else {
                // If the value wasn't provided, but it's required, error.
                if(requestResolver.isRequired()) {
                    logger.trace("Failed to resolve required property '{}' from request.", requestResolver.getPropertyName());
                    throw new RateLimitPropertyResolutionFailure(requestResolver);
                } else {
                    logger.trace("Optional property '{}' of request couldn't be resolved. Skipping.",
                            requestResolver.getPropertyName());
                }
            }
            // Note that if the property wasn't provided, but it was optional, it will be skipped from the result map.
        }
        return resolvedProperties;
    }

    /** Adds a resolver to the manager. */
    public void addResolver(RequestResolver resolver) {
        requestResolvers.add(resolver);
        logger.trace("Registered an additional request resolver of type '{}'.", resolver.getPropertyName());
    }

    @NotNull
    public List<RequestResolver> getResolvers() {
        return requestResolvers;
    }
}
