package org.springframework.samples.petclinic.visits.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MetricConfigTest {
    @Test void appliesApplicationTag() {
        var registry = new SimpleMeterRegistry();
        try {
            var config = new MetricConfig();
            config.metricsCommonTags().customize(registry);
            assertThat(registry.counter("sample").getId().getTag("application")).isEqualTo("petclinic");
        } finally { registry.close(); }
    }
    @Test void suppliesTimedAspect() {
        var registry = new SimpleMeterRegistry();
        try {
            assertThat(new MetricConfig().timedAspect(registry)).isNotNull();
        } finally { registry.close(); }
    }
}
