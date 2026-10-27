package com.bolaguard.core.testing;

import java.util.Map;

/**
 * Interface that the target application implements to provide test data 
 * to the BOLA-Guard framework.
 */
public interface BolaTestConfiguration {
    /**
     * @return A map of actor names (e.g., "OWNER", "VIEWER", "UNAUTHORIZED") to their JWT tokens.
     */
    Map<String, String> getActorTokens();

    /**
     * @return The ID of the object to test against (e.g., "101").
     */
    String getTestObjectId();
    
    /**
     * @return A map of actor names to the expected HTTP status for each HTTP method.
     * Example: Map.of("GET", Map.of("OWNER", 200, "VIEWER", 200, "UNAUTHORIZED", 403))
     */
    Map<String, Map<String, Integer>> getExpectedStatusMatrix();
    
    /**
     * @return Default JSON request bodies for endpoints that require them (like POST, PUT).
     */
    Map<String, String> getRequestBodies();

    /**
     * Optional hook called before each test scenario to restore test fixtures
     * and prevent cross-scenario side-effects (e.g. from DELETE or mutating operations).
     */
    default void resetTestData() {}
}
