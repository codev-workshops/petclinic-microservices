package org.springframework.samples.petclinic.api.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.PetDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer side of the customers-service contracts: the load-balanced {@code http://customers-service}
 * calls are routed by Spring Cloud LoadBalancer to the WireMock stubs published by customers-service.
 */
@SpringBootTest(properties = "spring.cloud.contract.stubrunner.ids-to-service-ids.spring-petclinic-customers-service=customers-service")
@ActiveProfiles("test")
@AutoConfigureStubRunner(
    ids = "org.springframework.samples.petclinic.client:spring-petclinic-customers-service:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
class CustomersServiceClientContractTest {

    @Autowired
    CustomersServiceClient customersServiceClient;

    @Test
    void getOwner_returnsOwnerWithPets() {
        OwnerDetails owner = customersServiceClient.getOwner(1).block();

        assertThat(owner).isNotNull();
        assertThat(owner.id()).isEqualTo(1);
        assertThat(owner.firstName()).isEqualTo("George");
        assertThat(owner.lastName()).isEqualTo("Franklin");
        assertThat(owner.address()).isEqualTo("110 W. Liberty St.");
        assertThat(owner.city()).isEqualTo("Madison");
        assertThat(owner.telephone()).isEqualTo("6085551023");
        assertThat(owner.getPetIds()).containsExactly(1);

        PetDetails pet = owner.pets().get(0);
        assertThat(pet.name()).isEqualTo("Leo");
        assertThat(pet.birthDate()).isEqualTo("2010-09-07");
        assertThat(pet.type().name()).isEqualTo("cat");
        assertThat(pet.visits()).isEmpty();
    }
}
