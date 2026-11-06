package org.springframework.samples.petclinic.api.application;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.samples.petclinic.api.dto.VisitDetails;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Consumer side of the visits-service contract: the load-balanced {@code http://visits-service}
 * calls are routed by Spring Cloud LoadBalancer to the WireMock stubs published by visits-service.
 */
@SpringBootTest(properties = "spring.cloud.contract.stubrunner.ids-to-service-ids.spring-petclinic-visits-service=visits-service")
@ActiveProfiles("test")
@AutoConfigureStubRunner(
    ids = "org.springframework.samples.petclinic.visits:spring-petclinic-visits-service:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
class VisitsServiceClientContractTest {

    @Autowired
    VisitsServiceClient visitsServiceClient;

    @Test
    void getVisitsForPets_returnsVisitsOfAllRequestedPets() {
        Visits visits = visitsServiceClient.getVisitsForPets(List.of(1, 2)).block();

        assertThat(visits).isNotNull();
        assertThat(visits.items())
            .extracting(VisitDetails::id, VisitDetails::petId, VisitDetails::date, VisitDetails::description)
            .containsExactly(
                tuple(1, 1, "2013-01-01", "rabies shot"),
                tuple(2, 2, "2013-01-02", "neutered"));
    }
}
