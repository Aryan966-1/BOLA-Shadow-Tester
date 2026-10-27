package com.bolaguard.core.discovery;

import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class DiscoveredEndpoint {
    private final String path;
    private final Set<RequestMethod> httpMethods;
    private final Class<?> controllerClass;
    private final Method handlerMethod;
    private final List<String> pathVariables;
    private final boolean requiresObjectAuthorization;
    private final String authorizationAnnotation;

    public DiscoveredEndpoint(String path, Set<RequestMethod> httpMethods, Class<?> controllerClass, 
                              Method handlerMethod, List<String> pathVariables, 
                              boolean requiresObjectAuthorization, String authorizationAnnotation) {
        this.path = path;
        this.httpMethods = httpMethods;
        this.controllerClass = controllerClass;
        this.handlerMethod = handlerMethod;
        this.pathVariables = pathVariables;
        this.requiresObjectAuthorization = requiresObjectAuthorization;
        this.authorizationAnnotation = authorizationAnnotation;
    }

    public String getPath() { return path; }
    public Set<RequestMethod> getHttpMethods() { return httpMethods; }
    public Class<?> getControllerClass() { return controllerClass; }
    public Method getHandlerMethod() { return handlerMethod; }
    public List<String> getPathVariables() { return pathVariables; }
    public boolean isRequiresObjectAuthorization() { return requiresObjectAuthorization; }
    public String getAuthorizationAnnotation() { return authorizationAnnotation; }

    @Override
    public String toString() {
        return "DiscoveredEndpoint{" +
                "path='" + path + '\'' +
                ", httpMethods=" + httpMethods +
                ", requiresObjectAuth=" + requiresObjectAuthorization +
                '}';
    }
}
