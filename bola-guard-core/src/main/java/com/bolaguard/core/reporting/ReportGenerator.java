package com.bolaguard.core.reporting;

import com.bolaguard.core.authorization.AuthorizationCoverageAnalyzer.CoverageResult;
import com.bolaguard.core.testing.TestResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {

    public void generateReport(String appName, List<CoverageResult> coverageResults, List<TestResult> testResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================\n");
        sb.append("                 BOLA-GUARD REPORT\n");
        sb.append("========================================================\n\n");
        sb.append("Application: ").append(appName).append("\n\n");

        long objectCentricCount = coverageResults.stream()
                .filter(c -> c.getEndpoint().isRequiresObjectAuthorization())
                .count();

        sb.append("Endpoints discovered: ").append(coverageResults.size()).append("\n");
        sb.append("Object-centric endpoints: ").append(objectCentricCount).append("\n\n");

        sb.append("Authorization Coverage\n");
        sb.append("----------------------\n\n");
        
        Map<String, Long> statusCounts = coverageResults.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));
                
        sb.append(String.format("Protected:       %d\n", statusCounts.getOrDefault("PROTECTED", 0L)));
        sb.append(String.format("Unprotected:     %d\n", statusCounts.getOrDefault("UNPROTECTED", 0L)));
        sb.append(String.format("Public:          %d\n", statusCounts.getOrDefault("PUBLIC", 0L)));
        sb.append(String.format("Unknown:         %d\n\n", statusCounts.getOrDefault("UNKNOWN", 0L)));

        long protectedCount = statusCounts.getOrDefault("PROTECTED", 0L);
        long unprotectedCount = statusCounts.getOrDefault("UNPROTECTED", 0L);
        long totalObj = protectedCount + unprotectedCount;
        int coverage = totalObj == 0 ? 100 : (int) ((protectedCount * 100) / totalObj);
        
        sb.append("Coverage: ").append(coverage).append("%\n\n");

        sb.append("Security Tests:\n");
        sb.append("---------------\n");
        for (TestResult r : testResults) {
            sb.append(String.format("  [%s] %s %s | Actor: %-15s | Expected: %d | Actual: %d | Classification: %s | %s%n",
                    r.isPassed() ? "PASS" : "FAIL",
                    r.getHttpMethod(), r.getEndpoint(), r.getActor(),
                    r.getExpectedStatus(), r.getActualStatus(),
                    r.getClassification(), r.getMessage()));
        }
        sb.append("---------------\n");
        sb.append("Total: ").append(testResults.size()).append("\n");
        
        long passed = testResults.stream()
                .filter(r -> r != null && r.isPassed())
                .count();
        long failed = testResults.size() - passed;
        
        long inconclusive = testResults.stream()
                .filter(r -> r != null && r.getClassification() == TestResult.Classification.INCONCLUSIVE)
                .count();
        long executionErrors = testResults.stream()
                .filter(r -> r != null && r.getClassification() == TestResult.Classification.TEST_EXECUTION_ERROR)
                .count();

        sb.append("Passed: ").append(passed).append("\n");
        sb.append("Failed: ").append(failed).append("\n");
        if (inconclusive > 0) {
            sb.append("Inconclusive: ").append(inconclusive).append("\n");
        }
        if (executionErrors > 0) {
            sb.append("Execution Errors: ").append(executionErrors).append("\n");
        }
        sb.append("\n");
        
        List<TestResult> bolaVulnerabilities = testResults.stream()
                .filter(r -> r != null && r.getClassification() == TestResult.Classification.BOLA_DETECTED)
                .collect(Collectors.toList());
                
        sb.append("BOLA vulnerabilities: ").append(bolaVulnerabilities.size()).append("\n\n");
        
        if (!bolaVulnerabilities.isEmpty()) {
            for (TestResult result : bolaVulnerabilities) {
                sb.append("[CRITICAL]\n");
                sb.append(result.getHttpMethod()).append(" ").append(result.getEndpoint()).append("\n");
                sb.append("Actor: ").append(result.getActor()).append("\n");
                sb.append("Expected: ").append(result.getExpectedStatus()).append("\n");
                sb.append("Actual: ").append(result.getActualStatus()).append("\n\n");
            }
            sb.append("========================================================\n");
            sb.append("BUILD STATUS: FAIL (BOLA DETECTED)\n");
            sb.append("========================================================\n");
        } else if (executionErrors > 0) {
            sb.append("========================================================\n");
            sb.append("BUILD STATUS: FAIL (SCANNER EXECUTION ERRORS)\n");
            sb.append("========================================================\n");
        } else {
            sb.append("========================================================\n");
            sb.append("BUILD STATUS: PASS\n");
            sb.append("========================================================\n");
        }
        
        System.out.println(sb.toString());
        
        if (!bolaVulnerabilities.isEmpty()) {
            throw new BolaVulnerabilityDetectedException("BOLA vulnerabilities detected: " + bolaVulnerabilities.size());
        }
    }
}
