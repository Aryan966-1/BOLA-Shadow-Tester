package com.bolaguard.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "BOLA Shadow Tester — Interactive Demo API",
        version = "1.0",
        description = """
            ## What this demo shows
            This sample file-sharing API demonstrates Broken Object Level Authorization (BOLA).
            Use the same file ID with different demo users to compare access decisions.

            ## Quick start
            1. Run the application with either the `demo-secure` or `demo-vulnerable` profile.
            2. Open **Authentication** and call `POST /api/auth/login` with one demo username.
            3. Copy the returned `token`.
            4. Click **Authorize** and paste the token only (do not add the word `Bearer`).
            5. Open **File operations** and try `GET /api/files/101`.
            6. Repeat the login and request as another user. Compare the HTTP status and response.

            ## Demo identities and sample data
            | Username | Relationship to file 101 | Expected secure read |
            | --- | --- | --- |
            | alice | Owner | 200 OK |
            | bob | Viewer | 200 OK |
            | charlie | No access | 403 Forbidden |
            | admin | Admin role; not automatically the file owner | Depends on object permission rules |

            File **101** belongs to Alice. Bob has VIEWER access. Charlie has no share.
            The database is seeded when the application starts.

            ## Secure vs vulnerable mode
            - `demo-secure`: object-level permission checks are enabled.
            - `demo-vulnerable`: the file-by-ID operations intentionally omit object-level checks.

            Restart the application with the other profile to switch modes. This is an intentionally simplified
            local teaching demo, not a production authentication system: login accepts a username without a password.
            Do not expose it to an untrusted network or use real data.
            """
    ),
    tags = {
        @Tag(name = "Authentication", description = "Get a demo JWT for Alice, Bob, Charlie, or Admin."),
        @Tag(name = "File operations", description = "Create, list, read, update, delete, and share demo files. Compare the same file ID across users.")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Paste the token returned by POST /api/auth/login. Swagger adds the Bearer prefix automatically."
)
public class OpenApiConfig {
}
