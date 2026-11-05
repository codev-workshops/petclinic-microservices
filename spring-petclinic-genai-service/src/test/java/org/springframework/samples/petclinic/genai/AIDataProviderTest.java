package org.springframework.samples.petclinic.genai;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.samples.petclinic.genai.dto.*;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIDataProviderTest {
    private final VectorStore store = mock(VectorStore.class);
    private final DiscoveryClient discovery = mock(DiscoveryClient.class);
    static List<Vet> vets() { return List.of(new Vet(1, "Jane", "Doe", Set.of())); }

    @ParameterizedTest @NullSource @MethodSource("vets")
    void searchesWithBoundedResults(Vet vet) {
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(new Document("vet")));
        var provider = new AIDataProvider(store, discovery);
        assertThat(provider.getVets(vet)).hasSize(1);
        verify(store).similaritySearch(argThat((SearchRequest request) ->
            request.getTopK() == (vet == null ? 50 : 20)));
    }
    @Test void emptySearchIsEmpty() {
        assertThat(new AIDataProvider(store, discovery).getVets(null)).isEmpty();
    }
    @Test void searchFailurePropagates() {
        when(store.similaritySearch(any(SearchRequest.class))).thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(() -> new AIDataProvider(store, discovery).getVets(null)).hasMessage("offline");
    }
    @Test void readsOwnersFromDiscoveredService() {
        RestClient client = mock(RestClient.class, RETURNS_DEEP_STUBS);
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(client);
        when(discovery.getInstances("customers-service")).thenReturn(List.of(
            new DefaultServiceInstance("id", "customers-service", "clinic", 8080, false)));
        var owners = List.of(new OwnerDetails(1, "Jane", "Doe", "Road", "Town", "123", List.of()));
        when(client.get().uri("http://clinic:8080/owners").retrieve()
            .body(any(ParameterizedTypeReference.class))).thenReturn(owners);
        try (var factory = mockStatic(RestClient.class)) {
            factory.when(RestClient::builder).thenReturn(builder);
            assertThat(new AIDataProvider(store, discovery).getAllOwners()).isSameAs(owners);
        }
    }
    @Test void createsPetAtDiscoveredService() {
        RestClient client = mock(RestClient.class, RETURNS_DEEP_STUBS);
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(client);
        when(discovery.getInstances("customers-service")).thenReturn(List.of(
            new DefaultServiceInstance("id", "customers-service", "clinic", 8080, false)));
        var request = new PetRequest(0, null, "Cat", 1);
        var pet = new PetDetails(2, "Cat", null, new PetType("cat"),
            List.of(new VisitDetails(1, 2, "2020-01-01", "checkup")));
        when(client.post().uri("http://clinic:8080/owners/1/pets").body(request)
            .retrieve().body(PetDetails.class)).thenReturn(pet);
        try (var factory = mockStatic(RestClient.class)) {
            factory.when(RestClient::builder).thenReturn(builder);
            assertThat(new AIDataProvider(store, discovery).addPetToOwner(1, request)).isSameAs(pet);
        }
    }
    @Test void createsOwnerAtDiscoveredService() {
        RestClient client = mock(RestClient.class, RETURNS_DEEP_STUBS);
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(client);
        when(discovery.getInstances("customers-service")).thenReturn(List.of(
            new DefaultServiceInstance("id", "customers-service", "clinic", 8080, false)));
        var request = new OwnerRequest("Jane", "Doe", "Road", "Town", "123");
        var owner = new OwnerDetails(1, "Jane", "Doe", "Road", "Town", "123", List.of());
        when(client.post().uri("http://clinic:8080/owners").body(request)
            .retrieve().body(OwnerDetails.class)).thenReturn(owner);
        try (var factory = mockStatic(RestClient.class)) {
            factory.when(RestClient::builder).thenReturn(builder);
            assertThat(new AIDataProvider(store, discovery).addOwnerToPetclinic(request)).isSameAs(owner);
        }
    }
    @Test void absentDiscoveryFailsBeforeHttp() {
        assertThatThrownBy(() -> new AIDataProvider(store, discovery).getAllOwners())
            .isInstanceOf(IndexOutOfBoundsException.class);
    }
}
