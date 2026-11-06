package org.springframework.samples.petclinic.genai;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.samples.petclinic.genai.dto.PetRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Consumer side of the customers-service contracts: {@link AIDataProvider} talks to the WireMock stubs
 * published by customers-service, discovered through a mocked {@link DiscoveryClient}.
 */
class AIDataProviderContractTest {

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
        .downloadStub("org.springframework.samples.petclinic.client", "spring-petclinic-customers-service", "+", "stubs")
        .stubsMode(StubRunnerProperties.StubsMode.CLASSPATH);

    AIDataProvider aiDataProvider;

    @BeforeEach
    void setUp() {
        URL stubUrl = stubRunner.findStubUrl("spring-petclinic-customers-service");
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        given(discoveryClient.getInstances("customers-service")).willReturn(List.of(
            new DefaultServiceInstance("customers-service-1", "customers-service", stubUrl.getHost(), stubUrl.getPort(), false)));
        aiDataProvider = new AIDataProvider(mock(VectorStore.class), discoveryClient);
    }

    @Test
    void getAllOwners() {
        List<OwnerDetails> owners = aiDataProvider.getAllOwners();

        assertThat(owners).extracting(OwnerDetails::id).containsExactly(1, 2);
        OwnerDetails george = owners.get(0);
        assertThat(george.firstName()).isEqualTo("George");
        assertThat(george.lastName()).isEqualTo("Franklin");
        assertThat(george.pets()).extracting(PetDetails::name).containsExactly("Leo");
        assertThat(george.pets().get(0).type().name()).isEqualTo("cat");
        assertThat(owners.get(1).pets()).extracting(PetDetails::name).containsExactly("Basil");
    }

    @Test
    void addOwnerToPetclinic() {
        OwnerDetails owner = aiDataProvider.addOwnerToPetclinic(
            new OwnerRequest("Jane", "Doe", "1 Main St.", "Springfield", "6085550000"));

        assertThat(owner.id()).isEqualTo(11);
        assertThat(owner.firstName()).isEqualTo("Jane");
        assertThat(owner.lastName()).isEqualTo("Doe");
        assertThat(owner.telephone()).isEqualTo("6085550000");
        assertThat(owner.pets()).isEmpty();
    }

    @Test
    void addPetToOwner() {
        Date birthDate = Date.from(LocalDate.of(2021, 4, 1).atStartOfDay(ZoneOffset.UTC).toInstant());

        PetDetails pet = aiDataProvider.addPetToOwner(1, new PetRequest(0, birthDate, "Max", 2));

        assertThat(pet.id()).isEqualTo(14);
        assertThat(pet.name()).isEqualTo("Max");
        assertThat(pet.birthDate()).isEqualTo("2021-04-01");
        assertThat(pet.type().name()).isEqualTo("dog");
    }
}
