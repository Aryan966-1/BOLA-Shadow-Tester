package com.bolaguard.core.authorization;

import com.bolaguard.core.discovery.DiscoveredEndpoint;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorizationCoverageAnalyzer {

    public enum EndpointStatus {
        PROTECTED,
        UNPROTECTED,
        PUBLIC,
        UNKNOWN
    }

    public static class CoverageResult {
        private final DiscoveredEndpoint endpoint;
        private final EndpointStatus status;

        public CoverageResult(DiscoveredEndpoint endpoint, EndpointStatus status) {
            this.endpoint = endpoint;
            this.status = status;
        }

        public DiscoveredEndpoint getEndpoint() { return endpoint; }
        public EndpointStatus getStatus() { return status; }
    }

    public List<CoverageResult> analyzeCoverage(List<DiscoveredEndpoint> endpoints) {
        return endpoints.stream()
                .map(this::analyzeEndpoint)
                .collect(Collectors.toList());
    }

    private CoverageResult analyzeEndpoint(DiscoveredEndpoint endpoint) {
        if (!endpoint.isRequiresObjectAuthorization()) {
            // Not object-centric (no path variable). Check if it has any auth.
            if (endpoint.getAuthorizationAnnotation() != null) {
                return new CoverageResult(endpoint, EndpointStatus.PROTECTED); // or PUBLIC/USER
            } else {
                return new CoverageResult(endpoint, EndpointStatus.PUBLIC);
            }
        }

        // It is object-centric (has path variable like {id})
        String auth = endpoint.getAuthorizationAnnotation();
        if (auth == null || auth.trim().isEmpty()) {
            return new CoverageResult(endpoint, EndpointStatus.UNPROTECTED);
        }
        
        // Simple heuristic: if it has @PreAuthorize and mentions hasPermission or similar, it's protected
        if (auth.contains("hasPermission") || auth.contains("hasRole") || auth.contains("hasAuthority") || auth.contains("hasAnyRole")) {
            return new CoverageResult(endpoint, EndpointStatus.PROTECTED);
        }

        return new CoverageResult(endpoint, EndpointStatus.UNKNOWN);
    }
}
