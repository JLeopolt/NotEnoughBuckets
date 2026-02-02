package net.pyroneon.NotEnoughBuckets.components;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.pyroneon.NotEnoughBuckets.flows.RateLimit;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Handles rate limits based on <code>@RateLimit</code> annotations. Rate limits can have different 'flows' rates, meaning
 * the capacity and speed of regeneration are different for certain methods.
 * <p>
 * Note this {@code HandlerInterceptor} will only apply to controller methods. Any other endpoints, such as those
 * handled by filters, should be manually rate limited via the {@code RateLimitService} or via a helper filter.
 */
public class RateLimitHandler implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    /**
     * @param rateLimitService The service which handles rate limiting.
     */
    public RateLimitHandler(RateLimitService rateLimitService){
        this.rateLimitService = rateLimitService;
    }


    /**
     * Handles rate limiting of a request method.
     * <p>
     * Keep the following in mind when adding rate limiting to your application:
     * <p>
     * 1.   If multiple rate limits are applied to a method, ensure they have different 'scopes' (properties they apply to).
     *      this ensures that rate limits aren't recomputed needlessly and have predictable behavior.
     *      <p>
     * 2.   When applying rate limits at the class and method level, only the method-level annotations will apply.
     *      <p>
     * 3.   Rate limits can be composited, and they will still be picked up by this handler. However, attempting to
     *      create composite rate limits of composited rate limits will not work.
     *      <p>
     * 4.   There is no method of 'precedence'. The handler doesn't care how restrictive a rate limit is when applying
     *      multiple. For this reason, do not try to combine overlapping rate limits.
     *      <p>
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        if(handler instanceof HandlerMethod handlerMethod) {
            // Get the qualified method name to ensure uniqueness within application
            String qualifiedMethod = getQualifiedMethodName(handlerMethod);

            // Check which rate limit(s) should be applied. (Supports composed annotations)
            List<RateLimit> effectiveRateLimits = getEffectiveRateLimits(handlerMethod);

            // Try applying the rate limit. If exceeded, an exception will be thrown.
            rateLimitService.apply(request, qualifiedMethod, effectiveRateLimits);
        }
        // Pass
        return true;
    }

    /** @return The fully qualified name of the class and method. */
    private String getQualifiedMethodName(HandlerMethod handlerMethod) {
        // Get the qualified class name (i.e., the controller class), include package
        String qualifiedClass = handlerMethod.getMethod().getDeclaringClass().getName();
        // Get the method name itself
        String method = handlerMethod.getMethod().getName();
        // Assume the class isn't static, so use # notation
        return qualifiedClass + "#" + method;
    }

    /**
     * Checks if the method or parent class has a RateLimit applied. Supports composed annotations.
     * Note that if the method has at least one rate limit, then any class-level rate limits will be ignored.
     * @return A list of RateLimits, which may be empty but not null.
     */
    @NotNull
    private List<RateLimit> getEffectiveRateLimits(HandlerMethod handlerMethod) {
        // Search at the method level
        Method method = handlerMethod.getMethod();
        List<RateLimit> result = findAllRateLimits(method);
        // If anything was found, return now and exclude class-level.
        if(!result.isEmpty()) return result;

        // Search at the class level
        Class<?> beanType = handlerMethod.getBeanType();
        result = findAllRateLimits(beanType);
        // If no rate limits found, return empty list.
        return result.isEmpty() ? new ArrayList<>() : result;
    }

    /** Gets all rate limit annotations applied to some element. Supports meta annotations. */
    private List<RateLimit> findAllRateLimits(AnnotatedElement element) {
        return new ArrayList<>(AnnotatedElementUtils.getMergedRepeatableAnnotations(element, RateLimit.class));
    }
}
