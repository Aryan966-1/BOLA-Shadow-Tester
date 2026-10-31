# BOLA-Guard

## 1. Project Overview
BOLA-Guard is an automated runtime enforcement and regression testing framework for REST APIs built on Spring Boot. It discovers object-centric endpoints, analyzes object-level authorization coverage, and generates cross-user security tests to detect Broken Object Level Authorization (BOLA) vulnerabilities.

## 2. BOLA Explanation
Broken Object Level Authorization (BOLA), also known as IDOR, occurs when an application fails to properly verify if an authenticated user is authorized to access a specific data object requested via an identifier (e.g., `GET /api/files/101`). An attacker can exploit this by simply changing the identifier to access another user's private data.

## 3. Architecture
BOLA-Guard uses a dual-module architecture separating the security testing framework from the target application.

```text
                ┌───────────────────────┐
                │   Spring Boot Demo    │
                │                       │
                │ Controllers           │
                │ Services              │
                │ JPA / PostgreSQL      │
                └──────────┬────────────┘
                           │
                           ▼
                ┌───────────────────────┐
                │      BOLA-Guard       │
                │                       │
                │ Endpoint Discovery    │
                │ Coverage Analysis     │
                │ Matrix Generator      │
                │ MockMvc Executor      │
                │ Runtime Guard         │
                │ Reporting             │
                └──────────┬────────────┘
                           │
                           ▼
                ┌───────────────────────┐
                │   Maven / CI Pipeline │
                │                       │
                │ PASS / BUILD FAIL     │
                └───────────────────────┘
```

## 4. Framework vs Demo Separation
- **bola-guard-core**: The white-box framework itself. Contains dynamic endpoint discovery, coverage analysis, automatic matrix generation, and execution logic.
- **bola-guard-demo**: The target application mimicking a Google Drive-like file sharing service. Contains realistic domain entities, REST APIs, JWT authentication, and Spring Security integrations.

## 5. Domain Model
- **User**: Represents a system user (Alice, Bob, Charlie).
- **FileResource**: A file containing sensitive content. Owned by a User.
- **FileShare**: A relationship mapping a User to a FileResource with specific permissions (VIEWER, EDITOR).

## 6. Security Model
The application uses JWT authentication. Authorization is handled via Spring Security's `@PreAuthorize` and a custom `PermissionEvaluator`.
- `READ`: Allowed for OWNER, VIEWER, EDITOR.
- `UPDATE`: Allowed for OWNER, EDITOR.
- `DELETE`, `SHARE`: Allowed for OWNER only.

## 7. Authorization Matrix
Testing File 101 (Owner: Alice, Viewer: Bob, Unauthorized: Charlie)

| Endpoint | Alice (OWNER) | Bob (VIEWER) | Charlie (UNAUTHORIZED) |
|---|---|---|---|
| GET /101 | 200 | 200 | 403 |
| PUT /101 | 200 | 403 | 403 |
| DELETE /101 | 204 | 403 | 403 |
| SHARE /101 | 200 | 403 | 403 |

## 8. Installation
```bash
git clone <repo>
cd bola-guard
mvn clean install
```

## 9. PostgreSQL Setup
The application requires PostgreSQL.
Connection details are in `application.yml` (default: jdbc:postgresql://localhost:5432/bola_guard, bola/bola).

## 10. Docker Setup
You can start the required database via Docker Compose:
```bash
docker-compose up -d
```

## 11. Running Secure Mode
```bash
cd bola-guard-demo
mvn spring-boot:run -Dspring-boot.run.profiles=demo-secure
```
*In this mode, object-level authorization is enforced.*

## 12. Running Vulnerable Mode
```bash
cd bola-guard-demo
mvn spring-boot:run -Dspring-boot.run.profiles=demo-vulnerable
```
*In this mode, endpoints lack authorization checks, allowing BOLA exploitation.*

## 13. Running Tests
Run tests in Secure mode (Expect PASS):
```bash
mvn test -Dspring.profiles.active=demo-secure
```

Run tests in Vulnerable mode (Expect FAIL):
```bash
mvn test -Dspring.profiles.active=demo-vulnerable
```

## 14. Swagger Demo Walkthrough
Swagger UI is accessible at:
`http://localhost:8080/swagger-ui/index.html`

The API documentation is grouped into **Authentication** and **File operations**. Follow this workflow:

1. Start the app in either `demo-secure` or `demo-vulnerable` mode.
2. Open **Authentication → POST /api/auth/login**.
3. Enter one seeded username: `alice`, `bob`, `charlie`, or `admin`. This local teaching shortcut does not check a password.
4. Copy the returned `token`, click **Authorize**, and paste the token only. Swagger supplies the `Bearer` prefix.
5. Open **File operations → GET /api/files/{fileId}**, enter `101`, and execute.
6. Log in as another user, authorize with that user's token, and repeat the same request.

File 101 belongs to Alice. Bob has VIEWER access. Charlie has no share. In secure mode, Alice and Bob should be able to read it while Charlie should receive 403. In vulnerable mode, Charlie can read it; that unexpected 200 demonstrates BOLA.

The demo login is intentionally simplified for local testing. Do not expose it to an untrusted network or use real data.


## 15. CI/CD
A GitHub Actions workflow is provided in `.github/workflows/bola-guard.yml`. It runs tests in both profiles to ensure the framework catches regressions. If BOLA is detected, the build fails.

## 16. Example BOLA Attack
In vulnerable mode:
1. Login as Charlie (ID: 3)
2. `GET /api/files/101` (Alice's file)
3. Result: `200 OK`. Charlie successfully accessed Alice's data.

## 17. Example BOLA-Guard Detection
```text
BOLA DETECTED: Endpoint /api/files/{fileId} on resource 101 accessed by UNAUTHORIZED. Expected 403, Actual 200. Unauthorized user can access another user's object.
BUILD STATUS: FAIL
```

## 18. Example Secure Result
```text
Security Tests:
Total: 12
Passed: 12
Failed: 0
BOLA vulnerabilities: 0
BUILD STATUS: PASS
```

## 19. Project Limitations
- **MVP Scope**: Does not scan arbitrary nested JSON object identifiers.
- **REST Focus**: Assumes simple RESTful patterns (`/resource/{id}`).
- **No External Crawling**: Relies on internal Spring context (white-box) rather than network spidering.

## 20. Future Work
- Support for complex JSON body traversal.
- GraphQL endpoint support.
- OpenFGA/Zanzibar integration.
