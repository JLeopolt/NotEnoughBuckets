package net.pyroneon.NotEnoughBuckets.tests.resolvers;

import jakarta.servlet.http.HttpServletRequest;
import net.pyroneon.NotEnoughBuckets.resolvers.RequestResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomHeaderResolver implements RequestResolver {

    public static final String PROPERTY_NAME = "test_custom_header";

    @Override
    @NotNull
    public String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Rate limit by the value of <code>custom_header</code> request header. (Optional)
     */
    @Override
    @Nullable
    public String resolve(HttpServletRequest request) {
        return request.getHeader("custom_header");
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
