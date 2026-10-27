package com.bolaguard.core.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.bolaguard.core")
@ConditionalOnProperty(prefix = "bola-guard", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BolaGuardAutoConfiguration {
}
