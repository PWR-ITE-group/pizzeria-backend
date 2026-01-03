package pl.edu.pwr.pizzeria.pizzeriabackend.util;

import java.util.UUID;

/**
 * Utility class for generating unique tracking tokens for orders.
 */
public class TrackingTokenGenerator {

    /**
     * Generates a unique tracking token using UUID.
     * The token is URL-safe and suitable for use in tracking URLs.
     *
     * @return A unique tracking token (UUID without dashes, 32 characters)
     */
    public static String generateToken() {
        UUID uuid = UUID.randomUUID();
        // Return UUID without dashes for shorter, cleaner URLs
        return uuid.toString().replace("-", "");
    }
}

