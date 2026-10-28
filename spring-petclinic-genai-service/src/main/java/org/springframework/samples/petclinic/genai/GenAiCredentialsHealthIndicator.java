package org.springframework.samples.petclinic.genai;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("genAiCredentials")
public class GenAiCredentialsHealthIndicator implements HealthIndicator {

    private static final String MISSING_CREDENTIALS_REASON =
        "No OpenAI/Azure OpenAI API key configured (set OPENAI_API_KEY or AZURE_OPENAI_KEY)";

    private final Environment environment;

    public GenAiCredentialsHealthIndicator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Health health() {
        if (isConfigured(environment.getProperty("OPENAI_API_KEY"))) {
            return Health.up().withDetail("provider", "openai").build();
        }
        if (isConfigured(environment.getProperty("AZURE_OPENAI_KEY"))) {
            return Health.up().withDetail("provider", "azure-openai").build();
        }
        return Health.outOfService().withDetail("reason", MISSING_CREDENTIALS_REASON).build();
    }

    private boolean isConfigured(String key) {
        return key != null && !key.isBlank() && !"demo".equalsIgnoreCase(key);
    }
}
