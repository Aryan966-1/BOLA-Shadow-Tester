package com.bolaguard.core.testing;

import com.bolaguard.core.discovery.DiscoveredEndpoint;
import org.springframework.web.bind.annotation.RequestMethod;

public class AuthorizationScenario {
    private final DiscoveredEndpoint endpoint;
    private final RequestMethod method;
    private final String actor;
    private final String actorToken;
    private final int expectedStatus;
    private final String objectId;
    private final String requestBody;

    public AuthorizationScenario(DiscoveredEndpoint endpoint, RequestMethod method, String actor, 
                                 String actorToken, int expectedStatus, String objectId, String requestBody) {
        this.endpoint = endpoint;
        this.method = method;
        this.actor = actor;
        this.actorToken = actorToken;
        this.expectedStatus = expectedStatus;
        this.objectId = objectId;
        this.requestBody = requestBody;
    }

    public DiscoveredEndpoint getEndpoint() { return endpoint; }
    public RequestMethod getMethod() { return method; }
    public String getActor() { return actor; }
    public String getActorToken() { return actorToken; }
    public int getExpectedStatus() { return expectedStatus; }
    public String getObjectId() { return objectId; }
    public String getRequestBody() { return requestBody; }
}
