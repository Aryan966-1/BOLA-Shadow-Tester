package com.bolaguard.core.discovery;

import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EndpointDiscoveryService {

    private final ApplicationContext applicationContext;

    public EndpointDiscoveryService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<DiscoveredEndpoint> discoverEndpoints() {
        List<DiscoveredEndpoint> endpoints = new ArrayList<>();
        
        // Find all RequestMappingHandlerMapping beans (usually just one)
        Map<String, RequestMappingHandlerMapping> mappings = 
                applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
                
        for (RequestMappingHandlerMapping mapping : mappings.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
            
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo info = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();
                
                Set<String> patterns = getPatterns(info);
                Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
                if (methods.isEmpty()) {
                    // Fallback to all standard methods if none specified
                    methods = new HashSet<>(Arrays.asList(RequestMethod.GET, RequestMethod.POST, 
                                RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH));
                }
                
                for (String path : patterns) {
                    // Skip error and actuator paths
                    if (path.startsWith("/error") || path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
                        continue;
                    }
                    
                    List<String> pathVariables = extractPathVariables(path);
                    boolean isObjectCentric = !pathVariables.isEmpty();
                    
                    String authAnnotation = getAuthorizationAnnotation(handlerMethod.getMethod());
                    
                    endpoints.add(new DiscoveredEndpoint(
                            path,
                            methods,
                            handlerMethod.getBeanType(),
                            handlerMethod.getMethod(),
                            pathVariables,
                            isObjectCentric,
                            authAnnotation
                    ));
                }
            }
        }
        
        return endpoints;
    }

    private Set<String> getPatterns(RequestMappingInfo info) {
        var pathPatternsCondition = info.getPathPatternsCondition();
        if (pathPatternsCondition != null) {
            Set<PathPattern> patterns = pathPatternsCondition.getPatterns();
            Set<String> patternStrings = new HashSet<>();
            for (PathPattern pattern : patterns) {
                if (pattern != null) {
                    patternStrings.add(pattern.getPatternString());
                }
            }
            return patternStrings;
        }
        var patternsCondition = info.getPatternsCondition();
        if (patternsCondition != null) {
            Set<String> patterns = patternsCondition.getPatterns();
            return patterns != null ? patterns : Collections.emptySet();
        }
        return Collections.emptySet();
    }

    private List<String> extractPathVariables(String path) {
        List<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^}]+)}");
        Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    private String getAuthorizationAnnotation(Method method) {
        if (method.isAnnotationPresent(PreAuthorize.class)) {
            return method.getAnnotation(PreAuthorize.class).value();
        }
        // Could also check class-level if needed, but for MVP method-level is focus
        if (method.getDeclaringClass().isAnnotationPresent(PreAuthorize.class)) {
            return method.getDeclaringClass().getAnnotation(PreAuthorize.class).value();
        }
        return null;
    }
}
