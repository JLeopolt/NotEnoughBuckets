package net.pyroneon.NotEnoughBuckets.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Caused by a potential misconfiguration in rate limit configuration.
 * Could be handled with a 500 (INTERNAL_SERVER_ERROR).
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RateLimitMisconfigurationException extends RuntimeException {

    public RateLimitMisconfigurationException(String message) {
        super(message);
    }
}
