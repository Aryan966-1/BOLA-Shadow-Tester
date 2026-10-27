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
import com.bolaguard.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BolaGuardIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = createContainer();

    private static PostgreSQLContainer<?> createContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15-alpine");
        container.withDatabaseName("bola_guard_test");
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
    private Environment env;

    @Autowired
    private DataSeeder dataSeeder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        dataSeeder.resetData();
    }

    @Test
    public void executeBolaGuardTests() {
        System.out.println("Starting BOLA-Guard Execution...");
        
        // 1. Discover Endpoints
        List<DiscoveredEndpoint> endpoints = discoveryService.discoverEndpoints();
        System.out.println("Discovered " + endpoints.size() + " endpoints.");
        
        // 2. Analyze Coverage
        List<CoverageResult> coverageResults = coverageAnalyzer.analyzeCoverage(endpoints);
        
        // 3. Generate Matrix
        List<AuthorizationScenario> scenarios = matrixGenerator.generateMatrix(endpoints, testConfiguration);
        System.out.println("Generated " + scenarios.size() + " test scenarios.");
        
        // 4. Execute Tests with fixture reset hook
        MatrixTestExecutor executor = new MatrixTestExecutor(mockMvc, testConfiguration);
        List<TestResult> testResults = executor.execute(scenarios);
        
        // 5. Generate Report and potentially fail build
        ReportGenerator reportGenerator = new ReportGenerator();
        
        boolean isVulnerableProfile = false;
        for (String profile : env.getActiveProfiles()) {
            if (profile.equals("demo-vulnerable")) {
                isVulnerableProfile = true;
                break;
            }
        }

        try {
            reportGenerator.generateReport("bola-guard-demo", coverageResults, testResults);
            
            // If we are in vulnerable profile, it SHOULD have thrown an exception
            if (isVulnerableProfile) {
                org.junit.jupiter.api.Assertions.fail("Expected BOLA vulnerabilities in vulnerable profile, but none were detected!");
            }
            // In secure profile, all 16 scenarios should pass with 0 failures
            assertEquals(0, testResults.stream().filter(r -> !r.isPassed()).count(),
                    "All test scenarios must pass in secure profile");
        } catch (BolaVulnerabilityDetectedException e) {
            // If we are in secure profile, it SHOULD NOT have thrown an exception
            if (!isVulnerableProfile) {
                org.junit.jupiter.api.Assertions.fail("Unexpected BOLA vulnerability in secure profile: " + e.getMessage());
            }
            System.out.println("Successfully detected BOLA in vulnerable profile!");
        }
    }

    @Test
    public void testOwnerCanAccessOwnObject() throws Exception {
        String aliceToken = jwtUtil.generateToken(1L, "alice", "USER");

        // Owner reading own file 101 -> 200 OK
        mockMvc.perform(get("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.ownerId").value(1));

        // Owner updating own file 101 -> 200 OK
        mockMvc.perform(put("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken)
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"name\":\"Updated by Alice\",\"content\":\"Confidential content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated by Alice"));

        // Owner deleting own file 101 -> 204 No Content
        mockMvc.perform(delete("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testOtherUserCannotAccessObjectWithoutPermission() throws Exception {
        String charlieToken = jwtUtil.generateToken(3L, "charlie", "USER");

        // Unauthorized user trying to read Alice's file 101 -> 403 Forbidden
        mockMvc.perform(get("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + charlieToken))
                .andExpect(status().isForbidden());

        // Unauthorized user trying to update Alice's file 101 -> 403 Forbidden
        mockMvc.perform(put("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + charlieToken)
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"name\":\"Hacked\",\"content\":\"Tampered\"}"))
                .andExpect(status().isForbidden());

        // Unauthorized user trying to delete Alice's file 101 -> 403 Forbidden
        mockMvc.perform(delete("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + charlieToken))
                .andExpect(status().isForbidden());

        // Unauthorized user trying to share Alice's file 101 -> 403 Forbidden
        mockMvc.perform(post("/api/files/101/share")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + charlieToken)
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"userId\":3,\"permission\":\"VIEWER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testViewerCanReadButCannotMutate() throws Exception {
        String bobToken = jwtUtil.generateToken(2L, "bob", "USER");

        // Bob has VIEWER share on file 101 -> 200 OK for GET
        mockMvc.perform(get("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isOk());

        // Bob cannot update file 101 -> 403 Forbidden
        mockMvc.perform(put("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken)
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"name\":\"Bob update\",\"content\":\"content\"}"))
                .andExpect(status().isForbidden());

        // Bob cannot delete file 101 -> 403 Forbidden
        mockMvc.perform(delete("/api/files/101")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isForbidden());

        // Bob cannot share file 101 -> 403 Forbidden
        mockMvc.perform(post("/api/files/101/share")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken)
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"userId\":3,\"permission\":\"VIEWER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUnauthenticatedAccessIsRejected() throws Exception {
        // Unauthenticated access to object endpoint -> 401 Unauthorized
        mockMvc.perform(get("/api/files/101"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/files/101")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"name\":\"Anonymous\",\"content\":\"anon\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/files/101"))
                .andExpect(status().isUnauthorized());

        // Non-object-centric protected endpoint -> 401 Unauthorized
        mockMvc.perform(get("/api/files"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPublicEndpointsAccessibleWithoutAuthentication() throws Exception {
        // Login with valid credentials -> 200 OK with token
        mockMvc.perform(post("/api/auth/login")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"username\":\"alice\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.username").value("alice"));

        // Login with unknown user -> 401 Unauthorized
        mockMvc.perform(post("/api/auth/login")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"username\":\"nonexistent\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
}
