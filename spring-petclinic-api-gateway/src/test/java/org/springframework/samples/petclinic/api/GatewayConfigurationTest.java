package org.springframework.samples.petclinic.api;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class GatewayConfigurationTest {
    @Test void suppliesRestTemplate() {
        assertThat(new ApiGatewayApplication().loadBalancedRestTemplate()).isNotNull();
    }
}
