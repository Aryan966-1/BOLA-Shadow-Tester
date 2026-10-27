package com.bolaguard.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bola-guard")
public class BolaGuardProperties {

    private boolean enabled = true;
    private Discovery discovery = new Discovery();
    private Testing testing = new Testing();
    private Runtime runtime = new Runtime();
    private Reporting reporting = new Reporting();
    private Ci ci = new Ci();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Discovery getDiscovery() { return discovery; }
    public void setDiscovery(Discovery discovery) { this.discovery = discovery; }
    public Testing getTesting() { return testing; }
    public void setTesting(Testing testing) { this.testing = testing; }
    public Runtime getRuntime() { return runtime; }
    public void setRuntime(Runtime runtime) { this.runtime = runtime; }
    public Reporting getReporting() { return reporting; }
    public void setReporting(Reporting reporting) { this.reporting = reporting; }
    public Ci getCi() { return ci; }
    public void setCi(Ci ci) { this.ci = ci; }

    public static class Discovery {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Testing {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Runtime {
        private boolean enabled = true;
        private boolean failOnMissingAuthorization = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isFailOnMissingAuthorization() { return failOnMissingAuthorization; }
        public void setFailOnMissingAuthorization(boolean failOnMissingAuthorization) { this.failOnMissingAuthorization = failOnMissingAuthorization; }
    }

    public static class Reporting {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Ci {
        private boolean failOnBola = true;
        public boolean isFailOnBola() { return failOnBola; }
        public void setFailOnBola(boolean failOnBola) { this.failOnBola = failOnBola; }
    }
}
