package net.pyroneon.NotEnoughBuckets.tests.resolvers;

import jakarta.servlet.http.HttpServletRequest;
import net.pyroneon.NotEnoughBuckets.resolvers.RequestResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserIdResolver implements RequestResolver {

    public static final String PROPERTY_NAME = "test_user_id";

    @Override
    @NotNull
    public String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Rate limit by the value of <code>user_id</code> request header. (Required)
     */
    @Override
    @Nullable
    public String resolve(HttpServletRequest request) {
        return request.getHeader("user_id");
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
