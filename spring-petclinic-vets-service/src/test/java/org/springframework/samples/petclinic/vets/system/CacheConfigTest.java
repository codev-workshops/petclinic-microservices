package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CacheConfigTest {
    @Test void cacheConfigurationCanBeCreated() {
        assertThat(new CacheConfig()).isNotNull();
    }
}
