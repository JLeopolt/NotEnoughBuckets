package net.pyroneon.NotEnoughBuckets.resolvers;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines some property of a request, and a method to extract it from a request.
 * For use in rate limiting, to target request attempts.
 * <p>
 * For convenience, define a public static final String member called "PROPERTY_NAME" which
 * contains the name of the property returned by the <code>#getPropertyName()</code> method.
 * This member can then be reused when creating RateLimit compositions.
 */
public interface RequestResolver {

    /** Returns the name of this resolvable property. Expected to be unique. */
    @NotNull String getPropertyName();

    /**
     * Resolves the specified property from a request.
     * @return The property value, or null if not provided and <code>isRequired()=false</code>.
     */
    @Nullable String resolve(HttpServletRequest request);

    /** True if this property is required. */
    boolean isRequired();
}
