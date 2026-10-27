package com.bolaguard.demo;

import com.bolaguard.core.authorization.AuthorizationCoverageAnalyzer;
import com.bolaguard.core.authorization.AuthorizationCoverageAnalyzer.CoverageResult;
import com.bolaguard.core.discovery.DiscoveredEndpoint;
import com.bolaguard.core.discovery.EndpointDiscoveryService;
import com.bolaguard.core.reporting.BolaVulnerabilityDetectedException;
import com.bolaguard.core.reporting.ReportGenerator;
import com.bolaguard.core.testing.AuthorizationScenario;
import com.bolaguard.core.testing.BolaTestConfiguration;
import com.bolaguard.core.testing.MatrixGenerator;
import com.bolaguard.core.testing.MatrixTestExecutor;
import com.bolaguard.core.testing.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("demo-vulnerable")
public class BolaGuardVulnerableIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = createContainer();

    private static PostgreSQLContainer<?> createContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15-alpine");
        container.withDatabaseName("bola_guard_vuln_test");
        container.withUsername("bola");
        container.withPassword("bola");
        return container;
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EndpointDiscoveryService discoveryService;

    @Autowired
    private AuthorizationCoverageAnalyzer coverageAnalyzer;

    @Autowired
    private MatrixGenerator matrixGenerator;

    @Autowired
    private BolaTestConfiguration testConfiguration;

    @Autowired
    private DataSeeder dataSeeder;

    @BeforeEach
    public void setUp() {
        dataSeeder.resetData();
    }

    @Test
    public void executeBolaGuardTests_detectsBOLAInVulnerableProfile() {
        System.out.println("Starting BOLA-Guard Execution on Vulnerable Profile...");

        List<DiscoveredEndpoint> endpoints = discoveryService.discoverEndpoints();
        List<CoverageResult> coverageResults = coverageAnalyzer.analyzeCoverage(endpoints);
        List<AuthorizationScenario> scenarios = matrixGenerator.generateMatrix(endpoints, testConfiguration);

        MatrixTestExecutor executor = new MatrixTestExecutor(mockMvc, testConfiguration);
        List<TestResult> testResults = executor.execute(scenarios);

        long bolaCount = testResults.stream()
                .filter(r -> r.getClassification() == TestResult.Classification.BOLA_DETECTED)
                .count();

        System.out.println("Vulnerable profile BOLA detections: " + bolaCount);
        assertTrue(bolaCount > 0, "Expected BOLA vulnerabilities in vulnerable profile, but none were detected!");

        ReportGenerator reportGenerator = new ReportGenerator();
        assertThrows(BolaVulnerabilityDetectedException.class, () ->
                reportGenerator.generateReport("bola-guard-demo-vulnerable", coverageResults, testResults));
    }
}
