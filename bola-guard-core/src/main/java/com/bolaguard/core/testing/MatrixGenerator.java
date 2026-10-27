package com.bolaguard.core.testing;

import com.bolaguard.core.discovery.DiscoveredEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MatrixGenerator {

    public List<AuthorizationScenario> generateMatrix(List<DiscoveredEndpoint> endpoints, BolaTestConfiguration config) {
        List<AuthorizationScenario> scenarios = new ArrayList<>();
        
        Map<String, String> tokens = config.getActorTokens();
        String objectId = config.getTestObjectId();
        Map<String, Map<String, Integer>> expectedMatrix = config.getExpectedStatusMatrix();
        Map<String, String> bodies = config.getRequestBodies();

        for (DiscoveredEndpoint endpoint : endpoints) {
            // Only generate matrix for object-centric endpoints
            if (endpoint.isRequiresObjectAuthorization() || endpoint.getPath().contains("{")) {
                Set<RequestMethod> methods = endpoint.getHttpMethods();
                for (RequestMethod method : methods) {
                    Map<String, Integer> methodExpectations = expectedMatrix.get(method.name());
                    
                    // Add an anonymous test case
                    String anonBody = bodies.getOrDefault(method.name(), "{}");
                    if (endpoint.getPath().endsWith("/share") && method == RequestMethod.POST) {
                        anonBody = bodies.getOrDefault("SHARE", "{\"userId\":3, \"permission\":\"VIEWER\"}");
                    }
                    scenarios.add(new AuthorizationScenario(
                            endpoint, method, "ANONYMOUS", null, 401, objectId, anonBody));

                    if (methodExpectations != null) {
                        for (Map.Entry<String, String> actorEntry : tokens.entrySet()) {
                            String actor = actorEntry.getKey();
                            String token = actorEntry.getValue();
                            Integer expectedStatus = methodExpectations.get(actor);
                            
                            if (expectedStatus != null) {
                                String body = bodies.getOrDefault(method.name(), "{}");
                                // For POST share, we need specific body
                                if (endpoint.getPath().endsWith("/share") && method == RequestMethod.POST) {
                                    body = bodies.getOrDefault("SHARE", "{\"userId\":3, \"permission\":\"VIEWER\"}");
                                }
                                
                                scenarios.add(new AuthorizationScenario(
                                        endpoint, method, actor, token, expectedStatus, objectId, body
                                ));
                            }
                        }
                    }
                }
            }
        }
        
        return scenarios;
    }
}
