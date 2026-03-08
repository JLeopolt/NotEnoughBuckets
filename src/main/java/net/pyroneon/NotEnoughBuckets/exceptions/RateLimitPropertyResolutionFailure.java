package net.pyroneon.NotEnoughBuckets.exceptions;

import net.pyroneon.NotEnoughBuckets.resolvers.RequestResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Caused when a requester doesn't provide enough information for the rate limit to be applied.
 * This should be handled with discretion. It could be handled with a 403 (FORBIDDEN) error,
 * or a 500 (INTERNAL_SERVER_ERROR) to indicate a potential misconfiguration.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RateLimitPropertyResolutionFailure extends RuntimeException {

    public RateLimitPropertyResolutionFailure(RequestResolver requestResolver) {
        super("Failed to resolve required request property '" + requestResolver.getPropertyName() + "' when applying rate limit.");
    }

    public RateLimitPropertyResolutionFailure(String message) {
        super(message);
    }
}
