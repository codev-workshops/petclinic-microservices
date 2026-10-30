package org.springframework.samples.petclinic.genai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenAiCredentialsHealthIndicatorTest {

    @Test
    void reportsOutOfServiceWhenNoCredentialsAreConfigured() {
        Health health = health(new MockEnvironment());

        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
    }

    @Test
    void reportsOutOfServiceWhenCredentialsAreBlank() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("OPENAI_API_KEY", " ")
            .withProperty("AZURE_OPENAI_KEY", "");

        assertEquals(Status.OUT_OF_SERVICE, health(environment).getStatus());
    }

    @Test
    void reportsOutOfServiceForDemoPlaceholder() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("OPENAI_API_KEY", "DeMo");

        assertEquals(Status.OUT_OF_SERVICE, health(environment).getStatus());
    }

    @Test
    void reportsOutOfServiceForSpringAiDemoPlaceholder() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("spring.ai.openai.api-key", "demo");

        assertEquals(Status.OUT_OF_SERVICE, health(environment).getStatus());
    }

    @Test
    void reportsOpenAiProviderWithoutExposingCredential() {
        String key = "openai-test-secret";
        MockEnvironment environment = new MockEnvironment()
            .withProperty("OPENAI_API_KEY", key);

        Health health = health(environment);

        assertEquals(Status.UP, health.getStatus());
        assertEquals("openai", health.getDetails().get("provider"));
        assertFalse(health.getDetails().toString().contains(key));
    }

    @Test
    void reportsOpenAiProviderFromSpringAiPropertyWithoutExposingCredential() {
        String key = "sk-x";
        MockEnvironment environment = new MockEnvironment()
            .withProperty("spring.ai.openai.api-key", key);

        Health health = health(environment);

        assertEquals(Status.UP, health.getStatus());
        assertEquals("openai", health.getDetails().get("provider"));
        assertFalse(health.getDetails().toString().contains(key));
    }

    @Test
    void ignoresUnresolvedSpringAiAzureProperty() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("spring.ai.azure.openai.api-key", "${AZURE_OPENAI_KEY}");

        assertEquals(Status.OUT_OF_SERVICE, health(environment).getStatus());
    }

    @Test
    void reportsAzureProviderWithoutExposingCredential() {
        String key = "azure-test-secret";
        MockEnvironment environment = new MockEnvironment()
            .withProperty("AZURE_OPENAI_KEY", key);

        Health health = health(environment);

        assertEquals(Status.UP, health.getStatus());
        assertEquals("azure-openai", health.getDetails().get("provider"));
        assertFalse(health.getDetails().toString().contains(key));
    }

    @Test
    void reportsAzureProviderFromSpringAiPropertyWithoutExposingCredential() {
        String key = "azure-test-secret";
        MockEnvironment environment = new MockEnvironment()
            .withProperty("spring.ai.azure.openai.api-key", key);

        Health health = health(environment);

        assertEquals(Status.UP, health.getStatus());
        assertEquals("azure-openai", health.getDetails().get("provider"));
        assertFalse(health.getDetails().toString().contains(key));
    }

    private Health health(MockEnvironment environment) {
        return new GenAiCredentialsHealthIndicator(environment).health();
    }
}
