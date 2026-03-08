package net.pyroneon.NotEnoughBuckets.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a request exceeds its rate limit.
 * Should return a 429 (TOO MANY REQUESTS) error.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(){
        super("You're issuing requests too quickly.");
    }

    public RateLimitExceededException(String message){
        super(message);
    }
}
