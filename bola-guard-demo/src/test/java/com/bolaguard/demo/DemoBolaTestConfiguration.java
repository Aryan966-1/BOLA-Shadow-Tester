package com.bolaguard.demo;

import com.bolaguard.core.testing.BolaTestConfiguration;
import com.bolaguard.demo.security.JwtUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DemoBolaTestConfiguration implements BolaTestConfiguration {

    private final JwtUtil jwtUtil;
    private final DataSeeder dataSeeder;

    public DemoBolaTestConfiguration(JwtUtil jwtUtil, DataSeeder dataSeeder) {
        this.jwtUtil = jwtUtil;
        this.dataSeeder = dataSeeder;
    }

    @Override
    public void resetTestData() {
        dataSeeder.resetData();
    }

    @Override
    public Map<String, String> getActorTokens() {
        Map<String, String> tokens = new HashMap<>();
        // ID 1 = Alice (OWNER), ID 2 = Bob (VIEWER), ID 3 = Charlie (UNAUTHORIZED)
        tokens.put("OWNER", jwtUtil.generateToken(1L, "alice", "USER"));
        tokens.put("VIEWER", jwtUtil.generateToken(2L, "bob", "USER"));
        tokens.put("UNAUTHORIZED", jwtUtil.generateToken(3L, "charlie", "USER"));
        return tokens;
    }

    @Override
    public String getTestObjectId() {
        return "101";
    }

    @Override
    public Map<String, Map<String, Integer>> getExpectedStatusMatrix() {
        Map<String, Map<String, Integer>> expectedMatrix = new HashMap<>();
        
        expectedMatrix.put("GET", Map.of(
            "OWNER", 200,
            "VIEWER", 200,
            "UNAUTHORIZED", 403
        ));
        
        expectedMatrix.put("PUT", Map.of(
            "OWNER", 200,
            "VIEWER", 403,
            "UNAUTHORIZED", 403
        ));
        
        expectedMatrix.put("DELETE", Map.of(
            "OWNER", 204, // or 200 depending on impl
            "VIEWER", 403,
            "UNAUTHORIZED", 403
        ));
        
        expectedMatrix.put("POST", Map.of(
            "OWNER", 200,
            "VIEWER", 403,
            "UNAUTHORIZED", 403
        ));
        
        return expectedMatrix;
    }

    @Override
    public Map<String, String> getRequestBodies() {
        Map<String, String> bodies = new HashMap<>();
        bodies.put("PUT", "{\"name\":\"Updated Name\", \"content\":\"Updated content\"}");
        // Specific body for SHARE will be handled by the generator logic
        bodies.put("SHARE", "{\"userId\":3, \"permission\":\"VIEWER\"}");
        return bodies;
    }
}
