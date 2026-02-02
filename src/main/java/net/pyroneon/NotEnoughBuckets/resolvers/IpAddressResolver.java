package net.pyroneon.NotEnoughBuckets.resolvers;

import jakarta.servlet.http.HttpServletRequest;
import net.pyroneon.NotEnoughBuckets.utils.RequestIpAddressResolver;
import org.jetbrains.annotations.NotNull;

public class IpAddressResolver implements RequestResolver {

    public static final String PROPERTY_NAME = "ip_address";

    @Override
    @NotNull
    public String getPropertyName() {
        return PROPERTY_NAME;
    }

    /** Attempts to resolve the IP address of a request. */
    @Override
    @NotNull
    public String resolve(HttpServletRequest request) {
        // Delegate to IP address resolver utility.
        return RequestIpAddressResolver.getClientIpAddress(request);
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
