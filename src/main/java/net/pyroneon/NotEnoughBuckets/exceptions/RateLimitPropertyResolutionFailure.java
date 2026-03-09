package net.pyroneon.NotEnoughBuckets.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * Caused when a request doesn't provide enough information for the rate limit to be applied.
 * This could be handled with a 403 (FORBIDDEN) error.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class RateLimitPropertyResolutionFailure extends RuntimeException {

    public RateLimitPropertyResolutionFailure(List<String> missingProperties) {
        super("Required request properties: [" + String.join(", ", missingProperties) + "] couldn't be resolved.");
    }

    public RateLimitPropertyResolutionFailure(String message) {
        super(message);
    }
}
