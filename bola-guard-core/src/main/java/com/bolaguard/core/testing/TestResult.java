package com.bolaguard.core.testing;

public class TestResult {
    private final String endpoint;
    private final String httpMethod;
    private final String actor;
    private final int expectedStatus;
    private final int actualStatus;
    private final boolean passed;
    private final Classification classification;
    private final String message;

    public enum Classification {
        AUTHORIZED_ACCESS,
        UNAUTHORIZED_ACCESS,
        BOLA_DETECTED,
        SECURE,
        AUTHENTICATION_FAILURE,
        TEST_EXECUTION_ERROR,
        CONFIGURATION_ERROR,
        UNSUPPORTED_ENDPOINT,
        INCONCLUSIVE,
        UNKNOWN
    }

    public TestResult(String endpoint, String httpMethod, String actor, int expectedStatus, 
                      int actualStatus, boolean passed, Classification classification, String message) {
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.actor = actor;
        this.expectedStatus = expectedStatus;
        this.actualStatus = actualStatus;
        this.passed = passed;
        this.classification = classification;
        this.message = message;
    }

    public String getEndpoint() { return endpoint; }
    public String getHttpMethod() { return httpMethod; }
    public String getActor() { return actor; }
    public int getExpectedStatus() { return expectedStatus; }
    public int getActualStatus() { return actualStatus; }
    public boolean isPassed() { return passed; }
    public Classification getClassification() { return classification; }
    public String getMessage() { return message; }
}
