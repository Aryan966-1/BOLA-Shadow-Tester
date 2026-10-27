package com.bolaguard.core.testing;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MatrixTestExecutor {

    private final MockMvc mockMvc;
    private final BolaTestConfiguration config;

    public MatrixTestExecutor(MockMvc mockMvc) {
        this(mockMvc, null);
    }

    public MatrixTestExecutor(MockMvc mockMvc, BolaTestConfiguration config) {
        this.mockMvc = mockMvc;
        this.config = config;
    }

    public List<TestResult> execute(List<AuthorizationScenario> scenarios) {
        List<TestResult> results = new ArrayList<>();
        
        for (AuthorizationScenario scenario : scenarios) {
            if (config != null) {
                config.resetTestData();
            }
            results.add(executeScenario(scenario));
        }
        
        return results;
    }

    private TestResult executeScenario(AuthorizationScenario scenario) {
        String path = scenario.getEndpoint().getPath();
        // Replace path variable placeholder with objectId
        path = path.replaceAll("\\{[^}]+}", scenario.getObjectId());
        
        HttpMethod method = scenario.getMethod().asHttpMethod();
        
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(method, Objects.requireNonNull(path));
        
        if (scenario.getActorToken() != null) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + scenario.getActorToken());
        }
        
        String requestBody = scenario.getRequestBody();
        if (requestBody != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
            requestBuilder.contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON));
            requestBuilder.content(Objects.requireNonNull(requestBody));
        }
        
        try {
            MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            int expectedStatus = scenario.getExpectedStatus();
            
            boolean passed = (actualStatus == expectedStatus) || 
                             (expectedStatus == 200 && actualStatus >= 200 && actualStatus < 300) ||
                             ("ANONYMOUS".equals(scenario.getActor()) && (expectedStatus == 401 || expectedStatus == 403) && (actualStatus == 401 || actualStatus == 403));

            if (actualStatus == 404 || actualStatus >= 500) {
                passed = false;
            }

            TestResult.Classification classification = classifyResult(scenario, actualStatus, passed);
            
            String message = passed ? "Test passed" : generateFailureMessage(scenario, actualStatus, classification);
            
            return new TestResult(
                    scenario.getEndpoint().getPath(),
                    method.name(),
                    scenario.getActor(),
                    expectedStatus,
                    actualStatus,
                    passed,
                    classification,
                    message
            );
            
        } catch (Exception e) {
            return new TestResult(
                    scenario.getEndpoint().getPath(),
                    method.name(),
                    scenario.getActor(),
                    scenario.getExpectedStatus(),
                    -1,
                    false,
                    TestResult.Classification.TEST_EXECUTION_ERROR,
                    "Exception during execution: " + e.getMessage()
            );
        }
    }
    
    private TestResult.Classification classifyResult(AuthorizationScenario scenario, int actualStatus, boolean passed) {
        if (!passed) {
            if ((scenario.getExpectedStatus() == 403 || scenario.getExpectedStatus() == 401) 
                    && (actualStatus >= 200 && actualStatus < 300)) {
                return TestResult.Classification.BOLA_DETECTED;
            }
            if (actualStatus == 401) {
                return TestResult.Classification.AUTHENTICATION_FAILURE;
            }
            if (actualStatus == 404 || (actualStatus >= 500 && actualStatus < 600)) {
                return TestResult.Classification.INCONCLUSIVE;
            }
            if (actualStatus == 403 && scenario.getExpectedStatus() == 200) {
                return TestResult.Classification.UNAUTHORIZED_ACCESS;
            }
            return TestResult.Classification.UNKNOWN;
        } else {
            if ((scenario.getExpectedStatus() == 403 || scenario.getExpectedStatus() == 401) 
                    && (actualStatus == 403 || actualStatus == 401)) {
                return TestResult.Classification.SECURE;
            }
            if (actualStatus >= 200 && actualStatus < 300) {
                return TestResult.Classification.AUTHORIZED_ACCESS;
            }
            return TestResult.Classification.UNKNOWN;
        }
    }
    
    private String generateFailureMessage(AuthorizationScenario scenario, int actualStatus, TestResult.Classification classification) {
        if (classification == TestResult.Classification.BOLA_DETECTED) {
            return String.format(
                    "BOLA DETECTED: Endpoint %s on resource %s accessed by %s. Expected %d, Actual %d. " +
                    "Unauthorized user can access another user's object.",
                    scenario.getEndpoint().getPath(), scenario.getObjectId(), scenario.getActor(), 
                    scenario.getExpectedStatus(), actualStatus);
        }
        if (classification == TestResult.Classification.INCONCLUSIVE) {
            return String.format(
                    "INCONCLUSIVE: Endpoint %s on resource %s returned %d for %s (Expected %d). Unable to reliably evaluate authorization.",
                    scenario.getEndpoint().getPath(), scenario.getObjectId(), actualStatus, scenario.getActor(), scenario.getExpectedStatus());
        }
        return String.format("Failed expectation for %s on %s. Expected %d, got %d", 
                scenario.getActor(), scenario.getEndpoint().getPath(), scenario.getExpectedStatus(), actualStatus);
    }
}
