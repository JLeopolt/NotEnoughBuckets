package net.pyroneon.NotEnoughBuckets.components;

import jakarta.servlet.http.HttpServletRequest;
import net.pyroneon.NotEnoughBuckets.exceptions.RateLimitMisconfigurationException;
import net.pyroneon.NotEnoughBuckets.resolvers.RequestResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Attempts to extract properties from an incoming HTTP request based on a configured set of resolvers.
 * Custom resolvers can be added or removed from the default internal resolvables list.
 */
public class RequestResolverManager {
    private static final Logger logger = LoggerFactory.getLogger(RequestResolverManager.class);

    // A modifiable map of resolvers' property names -> resolvers.
    @NotNull private final Map<String, RequestResolver> requestResolvers;

    public RequestResolverManager(RequestResolver... requestResolvers) {
        // Ensure the list remains mutable, so clients can add custom resolvers as needed.
        this.requestResolvers = new TreeMap<>();
        // Add all default resolvers, if any.
        for(RequestResolver resolver : requestResolvers) {
            addResolver(resolver);
        }
    }

    /**
     * Runs and aggregates results of all resolvers on the request, maps property name -> request's resolved value.
     * Note that if a property isn't required and is missing, no entry for it will exist in the resulting map.
     * @param applicableProperties The sanitized set of applicable properties.
     * @return A map of applicable property names -> the resolved values for this request.
     * If a key:value pair is missing, the value couldn't be resolved from request.
     * @throws RateLimitMisconfigurationException If a provided applicable property name was invalid.
     */
    @NotNull
    public Map<String, String> mapResolvedProperties(HttpServletRequest request, Set<String> applicableProperties) {
        Map<String, String> resolvedProperties = new TreeMap<>();
        for(String applicableProperty : applicableProperties) {
            RequestResolver requestResolver = requestResolvers.get(applicableProperty);
            // If no resolver exists for the applicable property, error
            if(requestResolver == null) {
                throw new RateLimitMisconfigurationException("The property '" + applicableProperty +
                        "' was requested by a rate limit, but no resolver exists for it.");
            }
            // Resolve the value of the property
            String resolvedValue = requestResolver.resolve(request);
            if(resolvedValue != null) {
                // Commit the property name and value to result map.
                resolvedProperties.put(requestResolver.getPropertyName(), resolvedValue);
                logger.trace("Resolved property '{}'='{}' in request.", requestResolver.getPropertyName(), resolvedValue);
            }
        }
        return resolvedProperties;
    }

    /**
     * Removes duplicate properties and includes the set of all properties if a wildcard exists.
     * Note that this method won't verify that properties are actually registered to a resolver.
     * @param appliesTo The raw output of a <code>RateLimit</code> <code>appliesTo</code> array.
     * @return A set of property names.
     */
    public Set<String> sanitizeApplicableProperties(String[] appliesTo) {
        // Convert to a set, automatically deleting duplicates
        Set<String> set = new HashSet<>(List.of(appliesTo));
        // If wildcard exists, replace the set with the complete properties set.
        if(set.contains("*")) {
            set = requestResolvers.keySet();
        }
        return set;
    }

    /**
     * Adds a resolver to the manager. It is recommended to add all resolvers prior to the first request being handled.
     */
    public void addResolver(RequestResolver resolver) {
        requestResolvers.put(resolver.getPropertyName(), resolver);
        logger.trace("Registered a request resolver of type '{}' and invalidated internal wildcard cache.", resolver.getPropertyName());
    }

    @NotNull
    public Map<String, RequestResolver> getResolvers() {
        return requestResolvers;
    }
}
