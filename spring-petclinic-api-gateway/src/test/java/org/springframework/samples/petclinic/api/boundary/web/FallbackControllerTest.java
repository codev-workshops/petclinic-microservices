package org.springframework.samples.petclinic.api.boundary.web;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FallbackControllerTest {
    @Test void signalsChatUnavailable() {
        var response = new FallbackController().fallback();
        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isEqualTo("Chat is currently unavailable. Please try again later.");
    }
}
