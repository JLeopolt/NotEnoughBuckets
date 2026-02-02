package net.pyroneon.NotEnoughBuckets.exceptions;

/**
 * This exception is thrown when a request exceeds its rate limit.
 * Should return a 429 (TOO MANY REQUESTS) error.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(){
        super("You're issuing requests too quickly.");
    }

    public RateLimitExceededException(String message){
        super(message);
    }
}
