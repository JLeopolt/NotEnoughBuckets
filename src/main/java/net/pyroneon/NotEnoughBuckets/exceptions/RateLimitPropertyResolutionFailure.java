package net.pyroneon.NotEnoughBuckets.exceptions;

import net.pyroneon.NotEnoughBuckets.resolvers.RequestResolver;

/**
 * Caused when a requester doesn't provide enough information for the rate limit to be applied.
 * This should be handled with discretion. It could be handled with a 403 (FORBIDDEN) error.
 */
public class RateLimitPropertyResolutionFailure extends RuntimeException {

    public RateLimitPropertyResolutionFailure(RequestResolver requestResolver) {
        super("Failed to resolve required request property '" + requestResolver.getPropertyName() + "' when applying rate limit.");
    }

    public RateLimitPropertyResolutionFailure(String message) {
        super(message);
    }
}
