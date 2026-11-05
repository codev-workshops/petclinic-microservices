package org.springframework.samples.petclinic.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import static org.mockito.Mockito.*;

class SpringBootAdminApplicationTest {
    @Test void delegatesToSpringBootWithoutStartingServices() {
        String[] args = {"--spring.cloud.config.enabled=false"};
        try (var spring = mockStatic(SpringApplication.class)) {
            SpringBootAdminApplication.main(args);
            spring.verify(() -> SpringApplication.run(SpringBootAdminApplication.class, args));
        }
    }
}
