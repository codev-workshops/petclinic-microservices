package org.springframework.samples.petclinic.genai;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.genai.dto.*;
import tools.jackson.core.JacksonException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetclinicToolsTest {
    private final AIDataProvider provider = mock(AIDataProvider.class);
    private final PetclinicTools tools = new PetclinicTools(provider);
    @Test void listsOwners() {
        var owners = List.of(new OwnerDetails(1, "Jane", "Doe", "Road", "Town", "123", List.of()));
        when(provider.getAllOwners()).thenReturn(owners);
        assertThat(tools.listOwners()).isSameAs(owners);
    }
    @Test void emptyOwnersRemainEmpty() {
        assertThat(tools.listOwners()).isEmpty();
    }
    @Test void createsOwner() {
        var request = new OwnerRequest("Jane", "Doe", "Road", "Town", "123");
        var owner = new OwnerDetails(1, "Jane", "Doe", "Road", "Town", "123", List.of());
        when(provider.addOwnerToPetclinic(request)).thenReturn(owner);
        assertThat(tools.addOwnerToPetclinic(request)).isSameAs(owner);
    }
    @Test void listsVets() {
        when(provider.getVets(null)).thenReturn(List.of("vet"));
        assertThat(tools.listVets(null)).containsExactly("vet");
    }
    @Test void invalidVetJsonReturnsEmpty() {
        when(provider.getVets(null)).thenThrow(mock(JacksonException.class));
        assertThat(tools.listVets(null)).isEmpty();
    }
    @Test void createsPet() {
        var request = new PetRequest(0, null, "Cat", 1);
        var pet = new PetDetails(2, "Cat", null, null, List.of());
        when(provider.addPetToOwner(1, request)).thenReturn(pet);
        assertThat(tools.addPetToOwner(1, request)).isSameAs(pet);
    }
}
