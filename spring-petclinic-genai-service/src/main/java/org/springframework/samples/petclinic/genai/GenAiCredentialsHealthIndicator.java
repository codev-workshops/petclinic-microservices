package org.springframework.samples.petclinic.genai;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("genAiCredentials")
public class GenAiCredentialsHealthIndicator implements HealthIndicator {

    private static final String MISSING_CREDENTIALS_REASON =
        "No OpenAI/Azure OpenAI API key configured (set spring.ai.openai.api-key in creds.yaml "
            + "or OPENAI_API_KEY, or spring.ai.azure.openai.api-key in creds.yaml or AZURE_OPENAI_KEY)";

    private final Environment environment;

    public GenAiCredentialsHealthIndicator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Health health() {
        if (isConfigured(getProperty("spring.ai.openai.api-key"))
            || isConfigured(getProperty("OPENAI_API_KEY"))) {
            return Health.up().withDetail("provider", "openai").build();
        }
        if (isConfigured(getProperty("spring.ai.azure.openai.api-key"))
            || isConfigured(getProperty("AZURE_OPENAI_KEY"))) {
            return Health.up().withDetail("provider", "azure-openai").build();
        }
        return Health.outOfService().withDetail("reason", MISSING_CREDENTIALS_REASON).build();
    }

    private String getProperty(String property) {
        try {
            return environment.getProperty(property);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isConfigured(String key) {
        return key != null
            && !key.isBlank()
            && !key.startsWith("${")
            && !"demo".equalsIgnoreCase(key);
    }
}
